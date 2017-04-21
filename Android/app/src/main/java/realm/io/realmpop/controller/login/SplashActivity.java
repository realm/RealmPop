package realm.io.realmpop.controller.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import realm.io.realmpop.R;
import realm.io.realmpop.controller.playername.PlayerNameActivity;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.util.SharedPrefsUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getName();
    private static final long SCHEMA_VERSION = 1L;
    private static String username() { return sharedPrefs.getPopUsername(); }
    private static String password() { return sharedPrefs.getPopPassword(); }
    private static String serverUrl() { return "http://" + sharedPrefs.getObjectServerHost() + ":9080"; }
    private static String realmUrl() { return "realm://" + sharedPrefs.getObjectServerHost() + ":9080/~/game"; }

    private static SharedPrefsUtils sharedPrefs = SharedPrefsUtils.getInstance();

    @BindView(R.id.hostIpTextView) public TextView rosIpTextView;
    @BindView(R.id.userTextView) public TextView userTextView;
    @BindView(R.id.passTextView) public TextView passTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        updateUIFromCachedRosConnectionInfo();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.connectToRosButton)
    public void login() {

        logoutExistingUser();
        updateRosConnectionInfoFromUI();
        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(username(), password(), false);

        SyncUser.loginAsync(syncCredentials, serverUrl(), new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) { postLogin(user); }

            @Override
            public void onError(ObjectServerError error) {
                logError(error);
            }
        });

    }

    private void updateUIFromCachedRosConnectionInfo() {
        rosIpTextView.setText(sharedPrefs.getObjectServerHost());
        userTextView.setText(sharedPrefs.getPopUsername());
        passTextView.setText(sharedPrefs.getPopPassword());
    }

    private void updateRosConnectionInfoFromUI() {
        sharedPrefs.setObjectServerHost(rosIpTextView.getText().toString());
        sharedPrefs.setPopUsername(userTextView.getText().toString());
        sharedPrefs.setPopPassword(passTextView.getText().toString());
    }

    private void logoutExistingUser() {
        SyncUser user = SyncUser.currentUser();
        if(user != null) {
            user.logout();
        }
    }

    private void postLogin(SyncUser user) {

        setRealmDefaultConfig(user);

        try(Realm realm = Realm.getDefaultInstance()) {
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

    }

    private void setRealmDefaultConfig(SyncUser user) {
        Log.d(TAG, "Connecting to Sync Server at : ["  + realmUrl().replaceAll("~", user.getIdentity()) + "]");
        Realm.removeDefaultConfiguration();
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
