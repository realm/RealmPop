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
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import realm.io.realmpop.R;
import realm.io.realmpop.util.GameHelpers;
import realm.io.realmpop.model.Game;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Score;
import realm.io.realmpop.model.Side;

import static realm.io.realmpop.util.RandomNumberUtils.generateNumber;

public class GameActivity extends AppCompatActivity {

    @BindView(R.id.playerLabel1) public TextView player1;
    @BindView(R.id.playerLabel2) public TextView player2;
    @BindView(R.id.message)      public TextView message;
    @BindView(R.id.timer)        public TextView timerLabel;
    @BindView(R.id.bubbleBoard)  public RelativeLayout bubbleBoard;

    private Realm realm;
    private Game challenge;
    private Side mySide;
    private Side otherSide;
    private Timer timer;
    private Date startedAt;
    private Player me;

    private boolean isGameOver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Setup & Bind Views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        message.setText("");
        startedAt = new Date();

        // Get default instance of realm and realm objects to use on the UI thread.
        realm = Realm.getDefaultInstance();
        me = GameHelpers.currentPlayer(realm);
        challenge = me.getCurrentgame();
        mySide = challenge.getPlayer1().getPlayerId().equals(me.getId()) ? challenge.getPlayer1() : challenge.getPlayer2();
        otherSide = challenge.getPlayer1().getPlayerId().equals(me.getId()) ? challenge.getPlayer2() : challenge.getPlayer1();
        isGameOver =  mySide.isFailed() || otherSide.isFailed();

        // Add a change listeners for the user on this device, to react to data changes.
        me.addChangeListener(new RealmChangeListener<Player>() {
            @Override
            public void onChange(Player me) {
                if(me.getCurrentgame() == null) {
                    finish();
                }
            }
        });

        RealmChangeListener<Side> onSideChangeListener = new RealmChangeListener<Side>() {
            @Override
            public void onChange(Side element) {
                if(isGameOver) {
                    mySide.removeChangeListeners();
                    otherSide.removeChangeListeners();
                } else {
                    update();
                }
            }
        };
        mySide.addChangeListener(onSideChangeListener);
        otherSide.addChangeListener(onSideChangeListener);
        Log.d("PLAYER_IDs: ", "[myId]" + me.getName());
        Log.d("PLAYER_IDs: ", "[mySideId]" + mySide.getName());
        Log.d("PLAYER_IDs: ", "[myOtherId]" + otherSide.getName());

        // Setup the bubble board views.
        setupBubbleBoard();
    }

    // Start the timer and update the view state on resume.
    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
        update();
    }

    // Stop the timer when we pause.
    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    // Remove all change listeners and close out the main realm instance when we're being destroyed.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(realm != null) {
            realm.removeAllChangeListeners();
            realm.close();
            realm = null;
        }
    }

    // When the user taps exit game.
    @OnClick(R.id.exitGameLabel)
    public void exitGame() {
        if(realm != null) {

            // Delete the game and associated objects asynchronously.
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {

                    Player me = GameHelpers.currentPlayer(bgRealm);
                    Player challenger = me.getChallenger();
                    Game game = me.getCurrentgame();
                    Side s1 = game.getPlayer1();
                    Side s2 = game.getPlayer2();
                    s1.deleteFromRealm();
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

    // When the user taps a bubble, react to it.
    public void onBubbleTap(final long numberTapped) {

        // Just to make sure, if there are none left to tap, exit.
        if (mySide.getLeft() <= 0) {
            return;
        }

        // Need to pass these objects across threads.
        final int bubble = challenge.getNumberArray()[(int)mySide.getLeft() - 1];
        final long currLeft = mySide.getLeft();
        final String mySidePlayerId = mySide.getPlayerId();

        // If the bubble that should have been tapped is the one the user on this device tapped,
        // then decrement the number left to tap.
        if(bubble == numberTapped) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    GameHelpers.sideWithPlayerId(mySidePlayerId, bgRealm).setLeft(currLeft - 1);
                }
            });

        // Otherwise, set player on this device as failed and update the message to indicate so on this device.
        } else {

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    GameHelpers.sideWithPlayerId(mySidePlayerId, bgRealm).setFailed(true);
                }

            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    message.setText("You tapped " + numberTapped + " instead of " + bubble);
                    message.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setupBubbleBoard() {

        float density  = 3.5f; //TODO: Clean up magic numbers
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        final int MAX_X_MARGIN =  Math.round(outMetrics.widthPixels - (100f * density));
        final int MAX_Y_MARGIN =  Math.round(outMetrics.heightPixels - (180f * density));

        for(final int bubbleNumber : challenge.getNumberArray()) {
            View bubbleView = getLayoutInflater().inflate(R.layout.bubble, bubbleBoard, false);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bubbleView.getLayoutParams();

            params.leftMargin = generateNumber(0, MAX_X_MARGIN);
            params.topMargin = generateNumber(0, MAX_Y_MARGIN);

            ((TextView) bubbleView.findViewById(R.id.bubbleValue)).setText(String.valueOf(bubbleNumber));
            bubbleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bubbleBoard.removeView(v);
                    onBubbleTap(bubbleNumber);
                }
            });

            bubbleBoard.addView(bubbleView, params);
        }
    }

    // Update the state of the UI and Game State if any major events occur each time this method is called.
    // It's called initially when the view is resumed, and then anytime one of the sides has a change.
    // Since it is reactive, we don't waste unnecessary cycles by calling it repeatedly on a timer or run loop.
    private void update() {

        final String mySidePlayerId = mySide.getPlayerId();
        final String otherSidePlayerId = otherSide.getPlayerId();


        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {

                Side mySide = GameHelpers.sideWithPlayerId(mySidePlayerId, bgRealm);
                Side otherSide = GameHelpers.sideWithPlayerId(otherSidePlayerId, bgRealm);

                if (mySide.getLeft() == 0) {

                    // MySide finished but time hasn't been recorded yet, so let's do that.
                    if (mySide.getTime() == 0) {
                        mySide.setTime(timeElapsed().getSeconds());
                    }

                    // Both side times have been recorded, the game is over.
                    if (otherSide.getTime() > 0 && mySide.getTime() > 0) {
                        isGameOver = true;
                        if (otherSide.getTime() < mySide.getTime()) {
                            mySide.setFailed(true);
                        } else {
                            otherSide.setFailed(true);
                            Score score = new Score();
                            score.setName(mySide.getName());
                            score.setTime(mySide.getTime());
                            bgRealm.copyToRealm(score);
                        }
                    }
                }
            }

        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {

                // Update UI
                player1.setText(challenge.getPlayer1().getName() + " : " + challenge.getPlayer1().getLeft());
                player2.setText(challenge.getPlayer2().getName() + " : " + challenge.getPlayer2().getLeft());

                if(mySide.getTime() > 0) {
                    message.setText(String.format("Your time: %s", timeElapsedString()));
                    message.setVisibility(View.VISIBLE);
                }

                if(otherSide.isFailed()) {
                    isGameOver = true;
                    stopTimer();
                    message.setText("You win! Congrats");
                    message.setVisibility(View.VISIBLE);
                } else if(mySide.isFailed()) {
                    isGameOver = true;
                    stopTimer();
                    if(TextUtils.isEmpty(message.getText())) {
                        message.setText("You lost!");
                    }
                    message.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private Period timeElapsed() {
        return new Interval(startedAt.getTime(), new Date().getTime()).toPeriod();
    }

    private String timeElapsedString() {
        Period period = timeElapsed();
        return String.format(Locale.US, "%02d:%02d", period.getMinutes(), (period.getSeconds() % 60));
    }

    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startTimer() {
        if(timer != null) {
            stopTimer();
        }
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
    }

}

