package realm.io.realmpop;

import android.app.Application;

import io.realm.Realm;
import realm.io.realmpop.util.SharedPrefsUtils;

public class RealmPopApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        SharedPrefsUtils.init(this);
    }


}
