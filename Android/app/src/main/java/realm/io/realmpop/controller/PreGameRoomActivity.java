package realm.io.realmpop.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import realm.io.realmpop.R;
import realm.io.realmpop.util.GameHelpers;
import realm.io.realmpop.model.Player;

import static realm.io.realmpop.util.BubbleConstants.AUTH_URL;
import static realm.io.realmpop.util.BubbleConstants.ID;
import static realm.io.realmpop.util.BubbleConstants.PASSWORD;
import static realm.io.realmpop.util.BubbleConstants.REALM_URL;

public class PreGameRoomActivity extends AppCompatActivity {

    private static final String TAG = PreGameRoomActivity.class.getName();

    @BindView(R.id.playerNameEditText) public EditText playerNameEditText;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Setup bindings to views.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregameroom);
        ButterKnife.bind(this);

        // Login to the sync server using pre-configured credentials
        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false);
        SyncUser.loginAsync(syncCredentials, AUTH_URL, new SyncUser.Callback() {

            // If login successful...
            @Override
            public void onSuccess(SyncUser user) {

                // Setup the SyncConfiguration & DefaultInstance configurations for Realm.
                final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
                Realm.setDefaultConfiguration(syncConfiguration);

                // Get an instance of realm to reference on the UI thread, update UI.
                realm = Realm.getDefaultInstance();
                playerNameEditText.setText(GameHelpers.currentPlayer(realm).getName());

                // Set myself as unavailable and my challenger to nothing because we're on the login screen,
                // we can't be challenged by anyone or available.
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        Player me = GameHelpers.currentPlayer(bgRealm);
                        me.setAvailable(false);
                        me.setChallenger(null);
                    }
                });
            }

            // If there was an error logging in, we may have wanted to do something more useful about it.
            @Override
            public void onError(ObjectServerError error) {
                Log.e(TAG, error.getErrorMessage());
                error.getException().printStackTrace();
            }
        });
    }


    // Make sure we remove all change listeners and close the realm instance on destory.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(realm != null) {
            realm.removeAllChangeListeners();
            realm.close();
            realm = null;
        }
    }

    // When the player presses enter...
    @OnClick(R.id.playerEnteredButton)
    public void onEnterPressed() {
        if(!isPlayerNameValid()) {
            shakeText();
        } else {
            moveToGameRoom();
        }
    }

    private void moveToGameRoom() {

        // Realm could be null, this might happen if we haven't heard back from our authenticate
        // call yet.
       if(realm != null) {

           // Get the player name from the text field.
           final String nameText = playerNameEditText.getText().toString();

           // Update the name of the player on this device in the background.
           realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Player me = GameHelpers.currentPlayer(bgRealm);
                    me.setName(nameText);
                }

           // afterward, on the foreground, lauch the GameRoomActivity.
           }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Intent gameRoomIntent = new Intent(PreGameRoomActivity.this, GameRoomActivity.class);
                    startActivity(gameRoomIntent);
                }
           });
        }
    }

    private void shakeText() {
        // do nothing for now.
    }

    private boolean isPlayerNameValid() {
        return !TextUtils.isEmpty(playerNameEditText.getText());
    }
}
