package realm.io.realmpop.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import realm.io.realmpop.R;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.util.SharedPrefsUtils;
import realm.io.realmpop.viewmodel.SplashVm;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashVm.class.getName();
    private static final String ID = "default@realm";
    private static final String PASSWORD = "password";
    private static final long SCHEMA_VERSION = 1L;
    private static String authUrl() { return "http://" + sharedPrefs.getRosAddress() + ":9080/auth"; }
    private static String realmUrl() { return "realm://" + sharedPrefs.getRosAddress() + ":9080/~/game"; }

    private static SharedPrefsUtils sharedPrefs = SharedPrefsUtils.getInstance();

    @BindView(R.id.hostIpTextView)
    private TextView rosIpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @OnClick(R.id.connectToRosButton)
    public void login() {

        logoutExistingUser();

        updateRosServerUrlFromUI();

        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false);

        SyncUser.loginAsync(syncCredentials, authUrl(), new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) { postLogin(user); }

            @Override
            public void onError(ObjectServerError error) {
                logError(error);
            }
        });

    }

    private void updateRosServerUrlFromUI() {
        sharedPrefs.setRosAddress(rosIpTextView.getText().toString());
    }

    private void logoutExistingUser() {
        SyncUser user = SyncUser.currentUser();
        if(user != null) {
            user.logout();
        }
    }

    public void postLogin(SyncUser user) {

        setRealmDefaultConfig(user);

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
                                          @Override
                                          public void execute(Realm bgRealm) {
                                              String playerId = SharedPrefsUtils.getInstance().idForCurrentPlayer();
                                              Player me = Player.byId(bgRealm, playerId);
                                              if(me == null) {
                                                  me = new Player();
                                                  me.setId(playerId);
                                                  me.setName("");
                                                  me = bgRealm.copyToRealmOrUpdate(me);
                                              }
                                              me.setAvailable(false);
                                              me.setChallenger(null);
                                              me.setCurrentGame(null);
                                          }
                                      },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() { goTo(PlayerNameActivity.class);
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        logError(error);
                    }
                });
    }

    private void setRealmDefaultConfig(SyncUser user) {
        Log.d(TAG, "Connecting to Sync Server at : ["  + realmUrl().replaceAll("~", user.getIdentity()) + "]");
        final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, realmUrl()).schemaVersion(SCHEMA_VERSION).build();
        Realm.setDefaultConfiguration(syncConfiguration);
    }

    @MainThread
    private void goTo(Class<? extends Activity> activity) {
        Intent intent = new Intent(SplashActivity.this, activity);
        startActivity(intent);
        finish();
    }

    @MainThread
    private void logError(Throwable error) {
        Log.e(TAG, "Error connecting", error);
        Toast.makeText(this, "Error connecting", Toast.LENGTH_SHORT).show();
    }


}
