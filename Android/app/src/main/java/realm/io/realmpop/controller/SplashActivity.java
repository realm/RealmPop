package realm.io.realmpop.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import realm.io.realmpop.R;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.util.GameHelpers;
import realm.io.realmpop.util.SharedPrefsUtils;

import static realm.io.realmpop.util.BubbleConstants.AUTH_URL;
import static realm.io.realmpop.util.BubbleConstants.ID;
import static realm.io.realmpop.util.BubbleConstants.PASSWORD;
import static realm.io.realmpop.util.BubbleConstants.REALM_URL;

public class SplashActivity extends BaseActivity {

    @BindView(R.id.splashText)
    public TextView splashText;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false);
        SyncUser.loginAsync(syncCredentials, AUTH_URL, new SyncUser.Callback() {

            @Override
            public void onSuccess(SyncUser user) {

                final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
                Realm.setDefaultConfiguration(syncConfiguration);
                realm = Realm.getDefaultInstance();

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        Player me = GameHelpers.currentPlayer(bgRealm);
                        if(me == null) {
                            me = new Player();
                            me.setId(SharedPrefsUtils.getInstance().idForCurrentPlayer());
                            me.setName("Anonymous");
                            me = bgRealm.copyToRealm(me);
                        }
                        me.setAvailable(false);
                        me.setChallenger(null);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(SplashActivity.this, PreGameRoomActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        logError(error);
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                logError(error);
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

    private void logError(Throwable error) {
        Log.e(this.getClass().getName(), error.getMessage());
        splashText.setText("You Died!");
    }

}
