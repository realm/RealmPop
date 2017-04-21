package realm.io.realmpop.controller.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
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

    private static final String TAG = "SplashActivity";
    private static final long SCHEMA_VERSION = 1L;
    private String username() { return sharedPrefs.getPopUsername(); }
    private String password() { return sharedPrefs.getPopPassword(); }
    private String serverUrl() { return "http://" + sharedPrefs.getObjectServerHost() + ":9080"; }
    private String realmUrl() { return "realm://" + sharedPrefs.getObjectServerHost() + ":9080/~/game"; }

    private SharedPrefsUtils sharedPrefs = SharedPrefsUtils.getInstance();

    @BindView(R.id.hostIpTextView)     public TextView rosIpTextView;
    @BindView(R.id.userTextView)       public TextView userTextView;
    @BindView(R.id.passTextView)       public TextView passTextView;
    @BindView(R.id.connectToRosButton) public Button loginButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        updateUIFromCachedRosConnectionInfo();

        progressDialog = new ProgressDialog(this, R.style.AppTheme_RealmPopDialog);//, R.style.CardView_Dark);//, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.login_connecting_text));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.connectToRosButton)
    public void login() {
        showProgress();
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
            realm.executeTransactionAsync(
                    new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            String playerId = SharedPrefsUtils.getInstance().idForCurrentPlayer();
                            Player me = Player.byId(bgRealm, playerId);
                            if (me == null) {
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
                        public void onSuccess() {
                            hideProgress();
                            goTo(PlayerNameActivity.class);
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
    private void logError(ObjectServerError error) {

        Log.e(TAG, "Error connecting", error);
        hideProgress();

        switch (error.getErrorCode()) {
            case INVALID_CREDENTIALS:
               showConnectionError("Error: Invalid Credentials...");
                break;
            default:
                showConnectionError("Error: Could not connect, check host...");
                break;
        }
    }

    private void showConnectionError(String error) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbar_container), error, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));
        snackbar.show();
    }

    private void showProgress() {
        loginButton.setEnabled(false);
        progressDialog.show();
    }

    private void hideProgress() {
        loginButton.setEnabled(true);
        progressDialog.dismiss();
    }

}
