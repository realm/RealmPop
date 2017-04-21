package realm.io.realmpop.controller.game;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmObjectChangeListener;
import realm.io.realmpop.R;
import realm.io.realmpop.controller.BaseAuthenticatedActivity;
import realm.io.realmpop.controller.gameroom.GameRoomActivity;
import realm.io.realmpop.controller.login.SplashActivity;
import realm.io.realmpop.model.Game;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Score;
import realm.io.realmpop.model.Side;
import realm.io.realmpop.util.GameTimer;
import realm.io.realmpop.util.PopUtils;

import static realm.io.realmpop.util.RandomNumberUtils.generateNumber;

public class GameActivity extends BaseAuthenticatedActivity implements GameTimer.TimerDelegate {

    @BindView(R.id.playerLabel1) public TextView player1;
    @BindView(R.id.playerLabel2) public TextView player2;
    @BindView(R.id.message)      public TextView message;
    @BindView(R.id.timer)        public TextView timerLabel;
    @BindView(R.id.bubbleBoard)  public RelativeLayout bubbleBoard;

    private Game challenge;
    private Side mySide;
    private Side otherSide;
    private Player me;

    private GameTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        message.setText("");
        timer = new GameTimer(PopUtils.GAME_LENGTH_SECONDS);

        me = Player.byId(getRealm(), getPlayerId());
        challenge = me.getCurrentGame();
        mySide = challenge.getPlayer1().getPlayerId().equals(me.getId()) ? challenge.getPlayer1() : challenge.getPlayer2();
        otherSide = challenge.getPlayer1().getPlayerId().equals(me.getId()) ? challenge.getPlayer2() : challenge.getPlayer1();

        setupBubbleBoard();
    }

    @Override
    protected void onResume() {
        super.onResume();

        me.addChangeListener(MeChangeListener);
        challenge.addChangeListener(GameChangeListener);
        mySide.addChangeListener(SideChangeListener);
        otherSide.addChangeListener(SideChangeListener);

        timer.startTimer(this);
        update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.stopTimer();
        removeAllChangeListenersFrom(me, challenge, mySide, otherSide);
    }

    @Override
    public void onBackPressed() {
        exitGameAfterDelay(0);
    }

    public void exitGameAfterDelay(final int delay) {
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Player.assignAvailability(true, mySide.getPlayerId());
                Player.assignAvailability(true, otherSide.getPlayerId());
            }
        }, delay);
    }

    @Override
    public void onTimerUpdate(final GameTimer.TimerEvent timerEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerLabel.setText(timerEvent.timeElapsedString);
            }
        });
    }

    @Override
    public void onTimeExpired(final GameTimer.TimeExpiredEvent timeExpiredEvent) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerLabel.setText(timeExpiredEvent.timeElapsedString);

                if (TextUtils.isEmpty(message.getText())) {
                    message.setText(R.string.game_outcome_message_timeout);
                }

                if(!challenge.isGameOver()) {
                    challenge.failUnfinishedSides();
                }
            }
        });
    }

    public void onBubbleTap(final long numberTapped) {

        // Just to make sure, if there are none left to tap, exit.
        if (mySide.getLeft() <= 0) {
            return;
        }

        final int numberThatShouldBeTapped = challenge.getNumberArray()[(int)mySide.getLeft() - 1];
        final long currLeft = mySide.getLeft();
        final String mySidePlayerId = mySide.getPlayerId();

        if(numberThatShouldBeTapped == numberTapped) {
            getRealm().executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Game currentGame = Player.byId(bgRealm, mySidePlayerId).getCurrentGame();
                    Side mySide = currentGame.sideWithPlayerId(mySidePlayerId);
                    mySide.setLeft(currLeft - 1);
                }
            });

        } else {

            getRealm().executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Game currentGame = Player.byId(bgRealm, mySidePlayerId).getCurrentGame();
                    Side mySide = currentGame.sideWithPlayerId(mySidePlayerId);
                    mySide.setFailed(true);
                }

            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    showMessage(getString(R.string.game_outcome_message_popoutoforder, numberTapped, numberThatShouldBeTapped));
                }
            });
        }
    }

    private void setupBubbleBoard() {

        if(bubbleBoard.getChildCount() > 0) {
            // Bubbles are drawn onStart.  If we have gone away from this view and comeback,
            // the game can still be in progress so we don't want to redraw bubbles.
            return;
        }

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

        if(me.getCurrentGame() == null) {
            return;
        }

        final String mySidePlayerId = mySide.getPlayerId();
        final String otherSidePlayerId = otherSide.getPlayerId();

        getRealm().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {

                Game currentGame = Player.byId(bgRealm, mySidePlayerId).getCurrentGame();
                Side mySide = currentGame.sideWithPlayerId(mySidePlayerId);
                Side otherSide = currentGame.sideWithPlayerId(otherSidePlayerId);

                if (mySide.getLeft() == 0) {

                    // MySide finished but time hasn't been recorded yet, so let's do that.
                    if (mySide.getTime() == 0) {
                        mySide.setTime(Double.valueOf(timerLabel.getText().toString()));
                        timer.expireInSeconds(20);
                    }

                    // Both side times have been recorded, the game is over.
                    if (otherSide.getTime() > 0 && mySide.getTime() > 0) {
                        if (otherSide.getTime() < mySide.getTime()) {
                            mySide.setFailed(true);
                        } else {
                            otherSide.setFailed(true);
                        }
                    }
                }
            }

        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {

                player1.setText(challenge.getPlayer1().getName() + " : " + challenge.getPlayer1().getLeft());
                player2.setText(challenge.getPlayer2().getName() + " : " + challenge.getPlayer2().getLeft());

                if(mySide.getTime() > 0) {
                    String finishTimeMsg = getString(R.string.game_outcome_message_finishtime, String.valueOf(mySide.getTime()));
                    showMessage(finishTimeMsg);
                }

                if(challenge.isGameOver()) {
                    timer.stopTimer();

                    if (mySide.isFailed()) {
                        if (TextUtils.isEmpty(message.getText())) {
                            message.setText(R.string.game_outcome_message_lose);
                        }
                        message.setVisibility(View.VISIBLE);

                    } else if (otherSide.isFailed()) {
                        showMessage(getString(R.string.game_outcome_message_win));
                        Score.addNewScore(mySide.getName(), mySide.getTime());
                    }

                    exitGameAfterDelay(5000);

                }
               }
        });
    }

    @MainThread
    private void showMessage(String text) {
        message.setText(text);
        message.setVisibility(View.VISIBLE);
    }

    private RealmObjectChangeListener<Game> GameChangeListener = new RealmObjectChangeListener<Game>() {
        @Override
        public void onChange(Game game, ObjectChangeSet objectChangeSet) {
        if (objectChangeSet.isDeleted() || !game.isValid()) {
            finish();
        }
        }
    };

    private RealmObjectChangeListener<Side> SideChangeListener = new RealmObjectChangeListener<Side>() {
        @MainThread
        @Override
        public void onChange(Side side, ObjectChangeSet objectChangeSet) {
            if(objectChangeSet.isDeleted() || !side.isValid()) {
                finish();
            }
            if(challenge.isGameOver()) {
                mySide.removeAllChangeListeners();
                otherSide.removeAllChangeListeners();
            }
            update();
        }
    };

    private RealmObjectChangeListener<Player> MeChangeListener = new RealmObjectChangeListener<Player>() {
        @MainThread
        @Override
        public void onChange(Player player, ObjectChangeSet objectChangeSet) {

            if(objectChangeSet.isDeleted() || !player.isValid()) {
               restartApp();

            } else if(me.getCurrentGame() == null) {
                finish();
            }
        }
    };

    private void removeAllChangeListenersFrom(RealmObject... realmObjects) {
        if(realmObjects != null) {
            for(RealmObject o : realmObjects) {
                if(o != null) {
                    o.removeAllChangeListeners();
                }
            }
        }
    }

}

