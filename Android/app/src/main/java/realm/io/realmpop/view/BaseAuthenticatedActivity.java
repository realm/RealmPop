package realm.io.realmpop.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.Map;

import io.realm.Realm;
import realm.io.realmpop.util.SharedPrefsUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

abstract public class BaseAuthenticatedActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRealm();
    }

    protected void closeRealm() {
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
        goTo(activity, null);
    }

    @MainThread
    protected void goTo(Class<? extends Activity> activity, Map<String, String> extras) {
        Intent intent = new Intent(this, activity);
        if (extras != null) {
            for (String extra : extras.keySet()) {
                intent.putExtra(extra, extras.get(extra));
            }
        }
        closeRealm();
        startActivity(intent);
        finish();
    }


}
