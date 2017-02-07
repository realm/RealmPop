package realm.io.realmpop.controller;

import android.content.Intent;
import android.os.Bundle;
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
import realm.io.realmpop.model.Player;
import realm.io.realmpop.util.GameHelpers;

import static realm.io.realmpop.util.BubbleConstants.AUTH_URL;
import static realm.io.realmpop.util.BubbleConstants.ID;
import static realm.io.realmpop.util.BubbleConstants.PASSWORD;
import static realm.io.realmpop.util.BubbleConstants.REALM_URL;

public class PreGameRoomActivity extends BaseActivity {

    private static final String TAG = PreGameRoomActivity.class.getName();

    @BindView(R.id.playerNameEditText) public EditText playerNameEditText;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregameroom);
        ButterKnife.bind(this);

        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false);
        SyncUser.loginAsync(syncCredentials, AUTH_URL, new SyncUser.Callback() {

            @Override
            public void onSuccess(SyncUser user) {

                final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
                Realm.setDefaultConfiguration(syncConfiguration);

                realm = Realm.getDefaultInstance();
                playerNameEditText.setText(GameHelpers.currentPlayer(realm).getName());

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        Player me = GameHelpers.currentPlayer(bgRealm);
                        me.setAvailable(false);
                        me.setChallenger(null);
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                Log.e(TAG, error.getErrorMessage());
                error.getException().printStackTrace();
            }
        });
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

    @OnClick(R.id.playerEnteredButton)
    public void onEnterPressed() {
        if(!isPlayerNameValid()) {
            shakeText();
        } else {
            moveToGameRoom();
        }
    }

    private void moveToGameRoom() {

       if(realm != null) {

           final String nameText = playerNameEditText.getText().toString();

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
