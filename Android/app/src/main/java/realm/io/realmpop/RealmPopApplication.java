package realm.io.realmpop;

import android.app.Application;

import io.realm.Realm;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import realm.io.realmpop.util.SharedPrefsUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class RealmPopApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmLog.setLevel(LogLevel.TRACE);
        SharedPrefsUtils.init(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/PressStart2P.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }


}
