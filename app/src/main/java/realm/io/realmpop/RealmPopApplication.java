package realm.io.realmpop;

import android.app.Application;

import io.realm.Realm;

public class RealmPopApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }


}
