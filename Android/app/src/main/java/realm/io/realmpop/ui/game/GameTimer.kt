package realm.io.realmpop.ui.game

import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class GameTimer(var expirationInSeconds: Long = 1L) {

    class TimerEvent(val timeElapsedString: String)
    class TimeExpiredEvent(val timeElapsedString: String)

    interface TimerDelegate {
        fun onTimerUpdate(timerEvent: TimerEvent)
        fun onTimeExpired(timeExpiredEvent: TimeExpiredEvent)
    }

    private val UPDATE_INTERVAL = 100L
    private var timer: Timer? = null
    private var startedAt: Date? = Date()

    private fun timeElapsedInSeconds() = timeElapsedInMillis() / 1000

    private fun timeElapsedInMillis(): Long {
        val localStartedAt = startedAt
        if (localStartedAt != null) {
            return Date().time - startedAt!!.time
        } else {
            return 0
        }
    }

    private fun timeElapsedString(): String {
        val totalTimeElapsedMillis = timeElapsedInMillis().toDouble()
        val asSeconds = totalTimeElapsedMillis / 1000.0
        return String.format(Locale.US, "%.1f", asSeconds)
    }

    private fun timeHasExpired(): Boolean {
        return timeElapsedInSeconds() >= expirationInSeconds
    }

    private fun isTimerRunning()= timer != null && startedAt != null

    fun stopTimer() {
        timer?.cancel()
        timer = null
        startedAt = null
    }

    fun startTimer(timerDelegate: TimerDelegate) {
        if (timer != null) {
            stopTimer()
        }
        timer = Timer()
        startedAt = Date()
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timerDelegate.onTimerUpdate(TimerEvent(timeElapsedString()))
                if (timeHasExpired()) {
                    timerDelegate.onTimeExpired(TimeExpiredEvent(timeElapsedString()))
                    stopTimer()
                }
            }
        }, Date(), UPDATE_INTERVAL)
    }

    fun expireInSeconds(endInSeconds: Int) {
        if (isTimerRunning()) {
            val previousExpirationTimeFromStart = expirationInSeconds
            val newExpirationTimeFromStart = timeElapsedInSeconds() + endInSeconds
            if (newExpirationTimeFromStart < previousExpirationTimeFromStart) {
                expirationInSeconds = newExpirationTimeFromStart
            }
        }
    }

}
