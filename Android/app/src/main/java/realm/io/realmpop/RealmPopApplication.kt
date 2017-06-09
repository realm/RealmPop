package realm.io.realmpop

import android.app.Application

import io.realm.Realm
import realm.io.realmpop.util.SharedPrefsUtils
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class RealmPopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        SharedPrefsUtils.init(this)
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/PressStart2P.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build())
    }


}
