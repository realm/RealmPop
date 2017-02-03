package realm.io.realmpop.controller;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.Seconds;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
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

        challenge = me.getCurrentgame();

        mySide = challenge.getPlayer1();
        mySide.addChangeListener(new RealmChangeListener<Side>() {
            @Override
            public void onChange(Side me) {
                update();
            }
        });

        otherSide = challenge.getPlayer2();
        otherSide.addChangeListener(new RealmChangeListener<Side>() {
            @Override
            public void onChange(Side other) {
                update();
            }
        });


//
// Some existing RelativeLayout from your layout xml
//        RelativeLayout rl = (RelativeLayout) findViewById(R.id.my_relative_layout);
//
//        ImageView iv = new ImageView(this);
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 40);
//        params.leftMargin = 50;
//        params.topMargin = 60;
//        rl.addView(iv, params);

//        update();

    }

    @Override
    protected void onResume() {
        super.onResume();

        startedAt = new Date();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Interval interval = new Interval(startedAt.getTime(), new Date().getTime());
                Period period = interval.toPeriod();
                final String timerText = String.format("%02d:%02d", period.getMinutes(), period.getSeconds());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerLabel.setText(timerText);
                    }
                });
            }
        }, now(), 1000);


        for(final Bubble bubble : mySide.getBubbles()) {
            View bubbleView = getLayoutInflater().inflate(R.layout.bubble, bubbleBoard, false);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bubbleView.getLayoutParams();
            DisplayMetrics metrics = new DisplayMetrics();

            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            params.leftMargin = bubbleView.getWidth() + generateNumber(0, metrics.widthPixels  -  bubbleView.getWidth());
            params.topMargin = bubbleView.getHeight() + generateNumber(0, metrics.heightPixels -  bubbleView.getHeight());


            ((TextView) bubbleView.findViewById(R.id.bubbleValue)).setText(bubble.getNumber() + "");
            bubbleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBubbleTap(bubble.getNumber());
                }
            });

            bubbleBoard.addView(bubbleView, params);
        }

        update();

    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();
        realm = null;
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

    public void onBubbleTap(long number) {

        Toast.makeText(this, "" + number, Toast.LENGTH_LONG).show();
//        try! challenge.realm?.write {
//            if let bubble = mySide.bubbles.last, bubble.number == number {
//                mySide.bubbles.removeLast()
//            } else {
//                message.isHidden = false
//                message.text = "You tapped \(number) instead of \(mySide.bubbles.last?.number ?? 0)"
//                mySide.failed = true
//                endGame()
//            }
//        }
//
    }

    private Date now() {
        return new Date();
    }

    private void update() {

        player1.setText(challenge.getPlayer1().getName() + " : " + challenge.getPlayer1().getBubbles().size());
        player2.setText(challenge.getPlayer2().getName() + " : " + challenge.getPlayer2().getBubbles().size());

        if(otherSide.isFailed()) {
            message.setText("You win! Congrats");
            message.setVisibility(View.VISIBLE);
        } else if(mySide.isFailed()) {
            message.setText("You lost!");
            message.setVisibility(View.VISIBLE);
        }

        if(mySide.getBubbles().size() > 0) {

            if(mySide.getTime() == 0) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mySide.setTime(System.currentTimeMillis());
                    }
                });
                message.setVisibility(View.VISIBLE);

            }

            if( otherSide.getTime() > 0 && mySide.getTime() > 0) {
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
    }
}

