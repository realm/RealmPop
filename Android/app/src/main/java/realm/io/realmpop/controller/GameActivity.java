package realm.io.realmpop.controller;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

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
import realm.io.realmpop.model.Game;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Score;
import realm.io.realmpop.model.Side;
import realm.io.realmpop.util.GameHelpers;

import static realm.io.realmpop.util.RandomNumberUtils.generateNumber;

public class GameActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        message.setText("");
        startedAt = new Date();

        realm = Realm.getDefaultInstance();
        me = GameHelpers.currentPlayer(realm);
        challenge = me.getCurrentgame();
        mySide = challenge.getPlayer1().getPlayerId().equals(me.getId()) ? challenge.getPlayer1() : challenge.getPlayer2();
        otherSide = challenge.getPlayer1().getPlayerId().equals(me.getId()) ? challenge.getPlayer2() : challenge.getPlayer1();

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
            if(isGameOver()) {
                mySide.removeChangeListeners();
                otherSide.removeChangeListeners();
            }
            update();
            }
        };
        mySide.addChangeListener(onSideChangeListener);
        otherSide.addChangeListener(onSideChangeListener);

        setupBubbleBoard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
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
    }

    @Override
    public void onBackPressed() {
        exitGameAfterDelay(0);
    }

    public void exitGameAfterDelay(int delay) {

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(realm != null) {

                    final String mySidePlayerId = mySide.getPlayerId();
                    final String otherSidePlayerId = otherSide.getPlayerId();

                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {

                            Player me = GameHelpers.playerWithId(mySidePlayerId, bgRealm);
                            Player other = GameHelpers.playerWithId(otherSidePlayerId, bgRealm);

                            other.setChallenger(null);
                            me.setChallenger(null);
                            other.setCurrentgame(null);
                            me.setCurrentgame(null);

                        }
                    });
                }
            }
        }, delay);
    }

    public void onBubbleTap(final long numberTapped) {

        // Just to make sure, if there are none left to tap, exit.
        if (mySide.getLeft() <= 0) {
            return;
        }

        final int bubble = challenge.getNumberArray()[(int)mySide.getLeft() - 1];
        final long currLeft = mySide.getLeft();
        final String mySidePlayerId = mySide.getPlayerId();

        if(bubble == numberTapped) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Game currentGame = GameHelpers.playerWithId(mySidePlayerId, bgRealm).getCurrentgame();
                    Side mySide = currentGame.sideWithPlayerId(mySidePlayerId);
                    mySide.setLeft(currLeft - 1);
                }
            });

        } else {

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Game currentGame = GameHelpers.playerWithId(mySidePlayerId, bgRealm).getCurrentgame();
                    Side mySide = currentGame.sideWithPlayerId(mySidePlayerId);
                    mySide.setFailed(true);
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

        Resources res = getResources();
        DisplayMetrics display = res.getDisplayMetrics();
        final int spaceTakenByButton = res.getDimensionPixelSize(R.dimen.bubble_button_diameter);
        final int titleBarHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));

        final int MAX_X_MARGIN = display.widthPixels - spaceTakenByButton; // bubble button diameter;

        final int MAX_Y_MARGIN =  display.heightPixels - (res.getDimensionPixelSize(R.dimen.activity_vertical_margin) //top margin
                                                        + res.getDimensionPixelSize(R.dimen.activity_vertical_margin) // bottom margin
                                                        + res.getDimensionPixelSize(R.dimen.realm_pop_status_bar_height) // pop status bar height
                                                        + titleBarHeight // android status bar height
                                                        + spaceTakenByButton); // bubble button diameter;

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

    private void update() {

        if(realm == null || me.getCurrentgame() == null) {
            return;
        }

        final String mySidePlayerId = mySide.getPlayerId();
        final String otherSidePlayerId = otherSide.getPlayerId();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {

                Game currentGame = GameHelpers.playerWithId(mySidePlayerId, bgRealm).getCurrentgame();
                Side mySide = currentGame.sideWithPlayerId(mySidePlayerId);
                Side otherSide = currentGame.sideWithPlayerId(otherSidePlayerId);

                if (mySide.getLeft() == 0) {

                    // MySide finished but time hasn't been recorded yet, so let's do that.
                    if (mySide.getTime() == 0) {
                        mySide.setTime(timeElapsed().getSeconds());
                    }

                    // Both side times have been recorded, the game is over.
                    if (otherSide.getTime() > 0 && mySide.getTime() > 0) {
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

                player1.setText(challenge.getPlayer1().getName() + " : " + challenge.getPlayer1().getLeft());
                player2.setText(challenge.getPlayer2().getName() + " : " + challenge.getPlayer2().getLeft());

                if(isGameOver()) {
                    stopTimer();
                    if(otherSide.isFailed()) {
                        showMessage("You win! Sweet");
                    } else if(mySide.isFailed()) {
                        if(TextUtils.isEmpty(message.getText())) {
                            message.setText("You lost!");
                        }
                        message.setVisibility(View.VISIBLE);
                    }
                    exitGameAfterDelay(5000);

                } else if(timeHasExpired()) {
                    // If time is expired and I have not got a time yet, I'm out of time.
                    if(mySide.getTime() == 0) {
                        stopTimer();
                        showMessage("You're out of time!");
                        exitGameAfterDelay(5000);

                    // Time expired and I've got time then I must have one.
                    } else {
                        stopTimer();
                        showMessage("You win! Sweet");
                        exitGameAfterDelay(5000);
                    }

                } else if(!timeHasExpired() && mySide.getTime() > 0) {
                    stopTimer();
                    showMessage(String.format("Your time: %s", timerLabel.getText()));
                    Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            failPlayer(otherSide.getPlayerId(), new Realm.Transaction.OnSuccess() {
                                @Override
                                public void onSuccess() {
                                    update();
                                }
                            });
                        }
                    }, 20000);
               }
            }
        });
    }

    private void showMessage(String text) {
        message.setText(text);
        message.setVisibility(View.VISIBLE);
    }

    private void failPlayer(final String playerId, Realm.Transaction.OnSuccess onSuccess) {

        if(onSuccess == null) {
            onSuccess = new Realm.Transaction.OnSuccess() { public void onSuccess() {} };
        }

        if(realm != null) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Game currentGame = GameHelpers.playerWithId(playerId, bgRealm).getCurrentgame();
                    if(currentGame != null) {
                        Side side = currentGame.sideWithPlayerId(playerId);
                        if(side != null) {
                            side.setFailed(true);
                        }
                    }
                }
            }, onSuccess);
        }
    }

    private boolean isGameOver() {
        return mySide.isFailed() || otherSide.isFailed();
    }

    private Period timeElapsed() {
        return new Interval(startedAt.getTime(), new Date().getTime()).toPeriod();
    }

    private String timeElapsedString() {
        Period period = timeElapsed();
        return String.format( Locale.US, "%.1f", period.getSeconds() + ((period.getMillis() / 1000d) % 60) );
    }

    private boolean timeHasExpired() {
        return timeElapsed().getMinutes() >= 1;
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
        startedAt = new Date();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String timerText = timeElapsedString();
                        if(timeHasExpired()) {
                            timerText = "60.0";
                            stopTimer();
                            update();
                        }
                        timerLabel.setText(timerText);
                    }
                });
            }
        }, new Date(), 100);
    }

}

