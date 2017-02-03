package realm.io.realmpop.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import realm.io.realmpop.R;
import realm.io.realmpop.model.GameModel;
import realm.io.realmpop.model.realm.Player;

import static realm.io.realmpop.util.RealmConstants.AUTH_URL;
import static realm.io.realmpop.util.RealmConstants.ID;
import static realm.io.realmpop.util.RealmConstants.PASSWORD;
import static realm.io.realmpop.util.RealmConstants.REALM_URL;

public class PreGameRoomActivity extends AppCompatActivity {

    private static final String TAG = PreGameRoomActivity.class.getName();

    private Realm realm;
    private GameModel gameModel;
    private Player me;

    @BindView(R.id.playerNameEditText)
    public EditText playerNameEditText;

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
                gameModel = new GameModel(realm);
                me = gameModel.currentPlayer();
                playerNameEditText.setText(me.getName());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
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
        gameModel = null;
        realm.close();
        realm = null;
    }

    @OnLongClick(R.id.player_name_prompt)
    public boolean clearAll() {
        if(realm != null) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgrealm) {
                    bgrealm.deleteAll();
                }
            });
            Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
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
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm r) {
                    me.setName(playerNameEditText.getText().toString());
                }
            });

            Intent gameRoomIntent = new Intent(PreGameRoomActivity.this, GameRoomActivity.class);
            startActivity(gameRoomIntent);
        }
    }

    private void shakeText() {
        // do nothing for now.
    }

    private boolean isPlayerNameValid() {
        return !TextUtils.isEmpty(playerNameEditText.getText());
    }
}
