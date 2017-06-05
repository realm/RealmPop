package realm.io.realmpop.controller;


import android.app.Activity;
import android.arch.lifecycle.LifecycleActivity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import realm.io.realmpop.controller.login.SplashActivity;
import realm.io.realmpop.util.SharedPrefsUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

abstract public class BaseAuthenticatedActivity extends LifecycleActivity {

    private Realm realm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeRealm();
    }

    private void closeRealm() {
        if (realm != null) {
            realm.removeAllChangeListeners();
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            realm.close();
            realm = null;
        }
    }

    protected Realm getRealm() {
        return realm;
    }

    protected String getPlayerId() {
        return SharedPrefsUtils.getInstance().idForCurrentPlayer();
    }

    @MainThread
    protected void goTo(Class<? extends Activity> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @MainThread
    protected void restartApp() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
