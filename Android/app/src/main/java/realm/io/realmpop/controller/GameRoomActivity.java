package realm.io.realmpop.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;
import okhttp3.Challenge;
import realm.io.realmpop.R;
import realm.io.realmpop.model.GameModel;
import realm.io.realmpop.model.realm.Bubble;
import realm.io.realmpop.model.realm.Game;
import realm.io.realmpop.model.realm.Player;
import realm.io.realmpop.model.realm.Side;

public class GameRoomActivity extends AppCompatActivity {

    private Realm realm;

    private GameModel gameModel;
    private Player me;
    RealmResults<Player> otherPlayers;

    @BindView(R.id.player_list)
    public RecyclerView recyclerView;

    private PlayerRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        gameModel = new GameModel(realm);
        me = gameModel.currentPlayer();
        otherPlayers = realm.where(Player.class).equalTo("available", true).findAllSortedAsync("name");
        recyclerView.setAdapter(new PlayerRecyclerViewAdapter(this, otherPlayers));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        me.addChangeListener(new RealmChangeListener<Player>() {
            @Override
            public void onChange(Player myself) {
                if(myself.getChallenger() != null) {
                    handleInvite(myself.getChallenger());
                }
                if(myself.getCurrentgame() != null) {
                    moveToGame(myself.getChallenger());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMyAvailability(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMyAvailability(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();
        realm = null;
        gameModel = null;
    }

    private void handleInvite(final Player challenger) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        createGame(challenger);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                me.setChallenger(null);
                            }
                        });
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You were invited to a game by " + challenger.getName() + "").setPositiveButton("Accept", dialogClickListener)
                .setNegativeButton("No, thanks", dialogClickListener).show();

        builder.show();

    }

    public void challengePlayer(final Player player) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                me.setChallenger(player);
            }
        });
    }

    private int[] generateNumbersArray(int count, int min, int max) {
        Random rand = new Random(System.currentTimeMillis());
        int[] numbers = new int[count];
        for(int i = 0; i < count; i++) {
            numbers[i] = rand.nextInt((max - min) + 1) + min;
        }
        return numbers;
    }

    private void createGame(final Player challenger) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                int [] numbers = generateNumbersArray(15, 1, 50);
                Game game = realm.createObject(Game.class);

                Side player1 = new Side();
                player1.setName(challenger.getName());
                player1 = realm.copyToRealm(player1);
                for(int i = 0; i < numbers.length; i++) {
                    Bubble bubble = realm.createObject(Bubble.class);
                    bubble.setNumber(numbers[i]);
                    player1.getBubbles().add(bubble);
                }
                game.setPlayer1(player1);


                Side player2 = new Side();
                player2.setName(me.getName());
                player2 = realm.copyToRealm(player2);
                for(int i = 0; i < numbers.length; i++) {
                    Bubble bubble = realm.createObject(Bubble.class);
                    bubble.setNumber(numbers[i]);
                    player2.getBubbles().add(bubble);
                }

                me.setCurrentgame(game);
                challenger.setCurrentgame(game);

            }
        });
    }

    private void moveToGame(Player challenger) {
        // do we need to set these to false or will the Game do that for us once started.
        me.setAvailable(false);
        challenger.setAvailable(false);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(Player.class.getName(), challenger.getId());
        startActivity(intent);
    }

    private void setMyAvailability(final boolean isAvail) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                me.setAvailable(isAvail);
            }
        });
    }
}
