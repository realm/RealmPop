package realm.io.realmpop.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import realm.io.realmpop.R;
import realm.io.realmpop.model.GameModel;
import realm.io.realmpop.model.realm.Game;
import realm.io.realmpop.model.realm.Player;
import realm.io.realmpop.model.realm.Score;
import realm.io.realmpop.model.realm.Side;

public class GameActivity extends AppCompatActivity {

    @BindView(R.id.playerLabel1)
    public TextView player1;

    @BindView(R.id.playerLabel2)
    public TextView player2;

    @BindView(R.id.message)
    public TextView message;

    @BindView(R.id.timer)
    public TextView timerLabel;

    private Realm realm;
    private GameModel gameModel;
    private Game challenge;

    private Side mySide;
    private Side otherSide;

    private Timer timer;
    private Date startedAt = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();
        gameModel = new GameModel(realm);

        String myId = getIntent().getStringExtra(Player.class.getName());
        challenge = realm.where(Player.class).equalTo("id", myId).findFirst().getCurrentgame();

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

//        //build UI
//        let playRect = view.bounds.insetBy(dx: 40, dy: 70).offsetBy(dx: 0, dy: 30)

//        for bubble in mySide.bubbles {
//            let bubbleView = BubbleView.bubble(number: bubble.number, inRect: playRect)
//            bubbleView.tap = {[weak self] number in
//            self?.didPop(number: number)
//            }
//            view.addSubview(bubbleView)
//        }

//
//        view.bringSubview(toFront: player1)
//        view.bringSubview(toFront: player2)
//        view.bringSubview(toFront: message)

        update();

    }

    @Override
    protected void onResume() {
        super.onResume();

        startedAt = new Date();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long intervalSinceStarted = System.currentTimeMillis() - startedAt.getTime();
                timerLabel.setText(String.format("%02.2d", intervalSinceStarted));
            }
        }, now(), 1000);


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
                // timer.invalidate??
                message.setText(String.format("Your time: %.2fs", mySide.getTime()));
                timerLabel.setText(String.format("%02.2f", mySide.getTime()));
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

