package realm.io.realmpop.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import realm.io.realmpop.util.GameHelpers;
import realm.io.realmpop.model.Game;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Side;
import realm.io.realmpop.util.BubbleConstants;

import static realm.io.realmpop.R.style.AppTheme_RealmPopDialog;
import static realm.io.realmpop.util.RandomNumberUtils.generateNumbersArray;

public class GameRoomActivity extends AppCompatActivity {

    @BindView(R.id.player_list) public RecyclerView recyclerView;

    private Realm realm;
    private Player me;
    private AtomicBoolean inGame = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Setup & Bind Views.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        ButterKnife.bind(this);

        // Get handle on a realm instance and my Player object on the main thread.
        realm = Realm.getDefaultInstance();
        me = GameHelpers.currentPlayer(realm);

        // Attach a listener for me to react to things like, handling an invite from someone
        // or moving to a game, if one has been setup.
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

        // Setup the recycler view.  Note here that we're performing this query Async and just passing
        // the results to the PlayerRecyclerViewAdapter.  The adapter will just react and update the
        // RecyclerView as results change, either from local changes, or changes over the network.
        RealmResults<Player> otherPlayers = realm.where(Player.class)
                .equalTo("available", true)
                .notEqualTo("id", me.getId())
                .findAllSortedAsync("name");
        recyclerView.setAdapter(new PlayerRecyclerViewAdapter(this, otherPlayers));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    // When we resume, we will set our availability to true.
    @Override
    protected void onResume() {
        super.onResume();
        setMyAvailability(true, null);
    }

    // When we pause, we will set our availability to false.
    @Override
    protected void onPause() {
        super.onPause();
        setMyAvailability(false, null);
    }

    // We will set our availability in the background.
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

    // Remember to always close your realm instances when you're done and remove all change listeners.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(realm != null) {
            realm.removeAllChangeListeners();
            realm.close();
            realm = null;
        }
    }

    // When someone invites us, we need to display a dialog to the user on this device, to see if they want to start the game.
    private void handleInvite(final Player challenger) {

        // Build a dialog click listener to handle the users choice.
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    // If the user on this device wants to accept and start a game.
                    case DialogInterface.BUTTON_POSITIVE:
                        // Let's create the game with this challenger
                        createGame(challenger);
                        break;

                    // If the user on this device declines let's remove the challenger.
                    case DialogInterface.BUTTON_NEGATIVE:
                        removeChallenger();
                        break;
                }
            }
        };

        // Build the dialog and show it.
        ContextThemeWrapper themedContext = new ContextThemeWrapper( this, AppTheme_RealmPopDialog );
        AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
        builder.setMessage("You were invited to a game by " + challenger.getName() + "")
                .setPositiveButton("Accept", dialogClickListener)
                .setNegativeButton("No, thanks", dialogClickListener)
                .show();
    }

    // When an item in the adapter is clicked, it will call this action and pass the player that was tapped
    // so we can react to the tap by challenging that player.
    public void challengePlayer(final Player player) {

        // We will set ourselves as the challenger for the other player.
        // Once that change makes it to the other player, they'll react to the challenge.
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

    // Remove the challenger from the player on this device.
    private void removeChallenger() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                GameHelpers.currentPlayer(bgRealm).setChallenger(null);
            }
        });
    }

    // Create a game between teh player on this device and the challenger.
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

    // Mark that we're in a game and move to that activity.
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

    // When the GameActivity finishes, we can set ourselves as available again.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inGame.set(false);
        setMyAvailability(true, null);
    }
}
