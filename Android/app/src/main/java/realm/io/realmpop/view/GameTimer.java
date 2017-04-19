package realm.io.realmpop.view;

import android.support.annotation.NonNull;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {

    private long updateInterval = 100;
    private int expirationInMinutes = 1;
    private Timer timer;
    private Date startedAt = new Date();

    public GameTimer() {}

    public GameTimer(int expirationInMinutes) {
        this.expirationInMinutes = expirationInMinutes;
    }

    private Period timeElapsed() {
        return new Interval(startedAt.getTime(), new Date().getTime()).toPeriod();
    }

    private String timeElapsedString() {
        Period period = timeElapsed();
        return String.format( Locale.US, "%.1f", period.getSeconds() + ((period.getMillis() / 1000d) % 60) );
    }

    private boolean timeHasExpired() {
        return timeElapsed().getMinutes() >= expirationInMinutes;
    }

    public void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void startTimer(@NonNull final TimerDelegate timerDelegate) {
        if(timer != null) {
            stopTimer();
        }
        timer = new Timer();
        startedAt = new Date();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerDelegate.onTimerUpdate(new TimerEvent(0, timeElapsedString()));
                if(timeHasExpired()) {
                    stopTimer();
                    timerDelegate.onTimeExpired(new TimeExpiredEvent());
                }
            }
        }, new Date(), updateInterval);
    }


    public class TimerEvent {
        public TimerEvent(long timeElapsed, String timeElapsedString) {
            this.timeElapsed = timeElapsed;
            this.timeElapsedString = timeElapsedString;
        }
        public long timeElapsed;
        public String timeElapsedString;
    }

    public class TimeExpiredEvent {
        public TimeExpiredEvent(){}
    }

    public interface TimerDelegate {
        void onTimerUpdate(TimerEvent timerEvent);
        void onTimeExpired(TimeExpiredEvent timeExpiredEvent);
    }
}
