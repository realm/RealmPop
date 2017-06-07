package realm.io.realmpop.controller.gameroom

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import realm.io.realmpop.util.playerDao

class AvailableHeartbeat : LifecycleObserver, AnkoLogger {

    private val HEARTBEAT_AVAIL_REFRESH_RATE = 20L * 1000L // 20 seconds.
    private var isAvailable: Boolean = false
    private val repeatSettingAvailable = Handler()
    private lateinit var SetAvailabilityTask: Runnable

    val setAvailabilityLamda = {
        debug(String.format("Set availability:[%b]", isAvailable))
        Realm.getDefaultInstance().use { r->
            r.playerDao(synchronous=true).assignAvailability(isAvailable=isAvailable)
        }
    }

    init {
        SetAvailabilityTask = Runnable {
            setAvailabilityLamda()
            repeatSettingAvailable.postDelayed(SetAvailabilityTask, HEARTBEAT_AVAIL_REFRESH_RATE)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        isAvailable = true
        repeatSettingAvailable.post(SetAvailabilityTask)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        isAvailable = false  // in case one final iteration runs, have it set to false on this.
        repeatSettingAvailable.removeCallbacks(SetAvailabilityTask)
        setAvailabilityLamda()
    }

}
