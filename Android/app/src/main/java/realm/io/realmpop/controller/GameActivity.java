package realm.io.realmpop.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import realm.io.realmpop.R;
import realm.io.realmpop.model.GameModel;
import realm.io.realmpop.model.realm.Bubble;
import realm.io.realmpop.model.realm.Game;
import realm.io.realmpop.model.realm.Player;
import realm.io.realmpop.model.realm.Score;
import realm.io.realmpop.model.realm.Side;

import static realm.io.realmpop.util.RandomUtils.generateNumber;

public class GameActivity extends AppCompatActivity {

    @BindView(R.id.playerLabel1)
    public TextView player1;

    @BindView(R.id.playerLabel2)
    public TextView player2;

    @BindView(R.id.message)
    public TextView message;

    @BindView(R.id.timer)
    public TextView timerLabel;

    @BindView(R.id.bubbleBoard)
    public RelativeLayout bubbleBoard;

    private Realm realm;
    private GameModel gameModel;
    private Game challenge;

    private Player me;

    private Side mySide;
    private Side otherSide;

    private Timer timer;
    private Date startedAt;

    private RealmChangeListener<Side> onSideChangeListener = new RealmChangeListener<Side>() {
        @Override
        public void onChange(Side element) {
            update();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();
        gameModel = new GameModel(realm);
        me = gameModel.currentPlayer();
        me.addChangeListener(new RealmChangeListener<Player>() {
            @Override
            public void onChange(Player me) {
                if(me.getCurrentgame() == null) {
                    finish();
                }
            }
        });

        message.setText("");

        challenge = me.getCurrentgame();

        mySide = challenge.getPlayer1().getName().equals(me.getName()) ? challenge.getPlayer1() : challenge.getPlayer2();
        mySide.addChangeListener(onSideChangeListener);

        otherSide = challenge.getPlayer1().getName().equals(me.getName()) ? challenge.getPlayer2() : challenge.getPlayer1();
        otherSide.addChangeListener(onSideChangeListener);

        //TODO: Need a distinct way other than name to determine player 1 vs player 2.
        Log.d("PLAYER_IDs: ", "[myId]" + me.getName());
        Log.d("PLAYER_IDs: ", "[mySideId]" + mySide.getName());
        Log.d("PLAYER_IDs: ", "[myOtherId]" + otherSide.getName());


        float density  = 3.5f; //TODO: Clean up magic numbers
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        final int MAX_X_MARGIN =  Math.round(outMetrics.widthPixels - (100f * density));
        final int MAX_Y_MARGIN =  Math.round(outMetrics.heightPixels - (180f * density));

        for(final Bubble bubble : mySide.getBubbles()) {
            View bubbleView = getLayoutInflater().inflate(R.layout.bubble, bubbleBoard, false);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bubbleView.getLayoutParams();

            params.leftMargin = generateNumber(0, MAX_X_MARGIN);
            params.topMargin = generateNumber(0, MAX_Y_MARGIN);

            ((TextView) bubbleView.findViewById(R.id.bubbleValue)).setText(String.valueOf(bubble.getNumber()));
            bubbleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bubbleBoard.removeView(v);
                    onBubbleTap(bubble.getNumber());
                }
            });

            bubbleBoard.addView(bubbleView, params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startedAt = new Date();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerLabel.setText(timeElapsedString());
                    }
                });
            }
        }, new Date(), 1000);

        update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(realm != null) {
            realm.removeAllChangeListeners();
            realm.close();
            realm = null;
        }
        gameModel = null;
    }

    @OnClick(R.id.exitGameLabel)
    public void exitGame() {
        if(gameModel != null && realm != null) {

            final String myId = me.getId();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {

                    Player me = bgRealm.where(Player.class).equalTo("id", myId).findFirst();
                    Player challenger = me.getChallenger();
                    Game game = me.getCurrentgame();
                    Side s1 = game.getPlayer1();
                    Side s2 = game.getPlayer2();

                    s1.getBubbles().deleteAllFromRealm();
                    s1.deleteFromRealm();
                    s2.getBubbles().deleteAllFromRealm();
                    s2.deleteFromRealm();
                    game.deleteFromRealm();

                    if(challenger != null) {
                        challenger.setCurrentgame(null);
                        challenger.setChallenger(null);
                    }

                    me.setCurrentgame(null);
                    me.setChallenger(null);
                }
            });
        }
    }

    public Period timeElapsed() {
        return new Interval(startedAt.getTime(), new Date().getTime()).toPeriod();
    }

    public String timeElapsedString() {
        Period period = timeElapsed();
        return String.format("%02d:%02d", period.getMinutes(), (period.getSeconds() % 60));
    }

    public void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void onBubbleTap(final long number) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Bubble> sortedBubbles = mySide.getBubbles().sort("number");
                Bubble bubble = sortedBubbles.last();
                if(bubble != null && bubble.getNumber() == number) {
                    mySide.getBubbles().remove(bubble);
                } else {
                    message.setText("You tapped " + number + " instead of " + (bubble == null ? 0 : bubble.getNumber()));
                    mySide.setFailed(true);
                    message.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void update() {

        player1.setText(challenge.getPlayer1().getName() + " : " + challenge.getPlayer1().getBubbles().size());
        player2.setText(challenge.getPlayer2().getName() + " : " + challenge.getPlayer2().getBubbles().size());

        if(mySide.getBubbles().size() == 0) {

            if(mySide.getTime() == 0) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mySide.setTime(timeElapsed().getSeconds());
                    }
                });
                message.setText(String.format("Your time: %s", timeElapsedString()));
                message.setVisibility(View.VISIBLE);
            }

            if( otherSide.getTime() > 0 && mySide.getTime() > 0) {
                mySide.removeChangeListeners();
                otherSide.removeChangeListeners();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if( otherSide.getTime() < mySide.getTime() ) {
                            mySide.setFailed(true);
                        } else {
                            otherSide.setFailed(true);
                            Score score = new Score();
                            score.setName(mySide.getName());
                            score.setTime(mySide.getTime());
                            realm.copyToRealm(score);
                        }
                    }
                });
            }

        }

        if(otherSide.isFailed()) {
            stopTimer();
            message.setText("You win! Congrats");
            message.setVisibility(View.VISIBLE);
        } else if(mySide.isFailed()) {
            stopTimer();
            if(TextUtils.isEmpty(message.getText())) {
                message.setText("You lost!");
            }
            message.setVisibility(View.VISIBLE);
        }

    }
}

