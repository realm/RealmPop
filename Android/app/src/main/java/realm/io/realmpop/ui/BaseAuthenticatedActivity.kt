package realm.io.realmpop.ui


import android.arch.lifecycle.LifecycleActivity
import android.content.Context
import android.content.Intent
import android.support.annotation.MainThread
import realm.io.realmpop.ui.login.SplashActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class BaseAuthenticatedActivity : LifecycleActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    @MainThread
    protected fun restartApp() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
