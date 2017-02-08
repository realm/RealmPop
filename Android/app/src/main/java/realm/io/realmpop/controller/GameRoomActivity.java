package realm.io.realmpop.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import realm.io.realmpop.R;
import realm.io.realmpop.model.Game;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Side;
import realm.io.realmpop.util.BubbleConstants;
import realm.io.realmpop.util.GameHelpers;

import static realm.io.realmpop.R.style.AppTheme_RealmPopDialog;
import static realm.io.realmpop.util.RandomNumberUtils.generateNumbersArray;

public class GameRoomActivity extends BaseActivity {

    @BindView(R.id.player_list) public RecyclerView recyclerView;

    private AlertDialog challengeDialog;

    private Realm realm;
    private Player me;
    private AtomicBoolean inGame = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();
        me = GameHelpers.currentPlayer(realm);

        me.addChangeListener(new RealmChangeListener<Player>() {
            @Override
            public void onChange(Player myself) {
                if(!inGame.get()) {
                    if(myself.getChallenger() != null) {
                        handleInvite(myself.getChallenger());
                    }
                    if(myself.getCurrentgame() != null) {
                        moveToGame();
                    }
                }
            }
        });

        RealmResults<Player> otherPlayers = realm.where(Player.class)
                .equalTo("available", true)
                .notEqualTo("id", me.getId())
                .findAllSortedAsync("name");
        recyclerView.setAdapter(new PlayerRecyclerViewAdapter(this, otherPlayers));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMyAvailability(true, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMyAvailability(false, null);
    }

    private void setMyAvailability(final boolean isAvail, Realm.Transaction.OnSuccess onSuccess) {

        if(onSuccess == null) {
            onSuccess = new Realm.Transaction.OnSuccess() {public void onSuccess() {}};
        }

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Player me = GameHelpers.currentPlayer(bgRealm);
                me.setAvailable(isAvail);
            }
        }, onSuccess);
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

    private void handleInvite(final Player challenger) {

        if(challengeDialog == null) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){

                        case DialogInterface.BUTTON_POSITIVE:
                            createGame(challenger);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            removeChallenger();
                            break;

                    }
                }
            };

            ContextThemeWrapper themedContext = new ContextThemeWrapper( this, AppTheme_RealmPopDialog );
            AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
            challengeDialog = builder.setMessage("You were invited to a game by " + challenger.getName() + "")
                    .setPositiveButton("Accept", dialogClickListener)
                    .setNegativeButton("No, thanks", dialogClickListener).create();
        }

        if(!challengeDialog.isShowing()) {
            challengeDialog.show();
        }

    }

    public void challengePlayer(final Player player) {

        if(player.isAvailable()) {

            final String otherPlayerId = player.getId();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Player me = GameHelpers.currentPlayer(bgRealm);
                    Player otherPlayer = GameHelpers.playerWithId(otherPlayerId, bgRealm);
                    otherPlayer.setChallenger(me);
                }
            });
        }

    }

    private void removeChallenger() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                GameHelpers.currentPlayer(bgRealm).setChallenger(null);
            }
        });
    }

    private void createGame(final Player challenger) {

        final String myId = me.getId();
        final String challengerId = challenger.getId();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {

                // Set myself and my challenger to unavailable.
                Player me = GameHelpers.playerWithId(myId, bgRealm);
                Player challenger = GameHelpers.playerWithId(challengerId, bgRealm);
                me.setAvailable(false);
                challenger.setAvailable(false);

                // Create a new game object
                Game game = bgRealm.createObject(Game.class);

                // Generate numbers for the bubbles and set on game.
                int [] numbers = generateNumbersArray(BubbleConstants.bubbleCount, 1, BubbleConstants.bubbleValueMax);
                game.setNumberArray(numbers);

                // Create side 1
                Side player1 = new Side();
                player1.setPlayerId(me.getId());
                player1.setName(me.getName());
                player1.setLeft(numbers.length);
                player1 = bgRealm.copyToRealm(player1);
                game.setPlayer1(player1);

                // Create side 2
                Side player2 = new Side();
                player2.setPlayerId(challenger.getId());
                player2.setName(challenger.getName());
                player2.setLeft(numbers.length);
                player2 = bgRealm.copyToRealm(player2);
                game.setPlayer2(player2);

                // Set the game object on both players.  They will react to this and automatically transition to the game.
                me.setCurrentgame(game);
                challenger.setCurrentgame(game);
            }
        });

    }

    private void moveToGame() {
        if(inGame.compareAndSet(false, true)) {
            setMyAvailability(false, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(GameRoomActivity.this, GameActivity.class);
                    intent.putExtra(Player.class.getName(), me.getId());
                    startActivityForResult(intent, 1);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inGame.set(false);
        setMyAvailability(true, null);
    }
}
