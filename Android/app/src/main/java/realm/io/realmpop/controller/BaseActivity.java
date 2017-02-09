package realm.io.realmpop.controller;


import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static realm.io.realmpop.util.BubbleConstants.REALM_URL;

abstract public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static void setActiveUser(SyncUser user) {
        final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
        Realm.setDefaultConfiguration(syncConfiguration);
    }

}
