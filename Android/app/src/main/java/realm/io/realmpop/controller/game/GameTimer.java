package realm.io.realmpop.controller.game;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {

    private static final long UPDATE_INTERVAL = 100;

    private long expirationInSeconds = 1;
    private Timer timer;
    private Date startedAt = new Date();

    public GameTimer(int expirationInSeconds) {
        this.expirationInSeconds = expirationInSeconds;
    }

    private long timeElapsedInSeconds() {
        return timeElapsedInMillis() / 1000;
    }

    private long timeElapsedInMillis() {
        return new Date().getTime() - startedAt.getTime();
    }

    private String timeElapsedString() {
        double totalTimeElapsedMillis = timeElapsedInMillis();
        double asSeconds = totalTimeElapsedMillis / 1000d;
        return String.format( Locale.US, "%.1f", asSeconds );
    }

    private boolean timeHasExpired() {
        return timeElapsedInSeconds() >= expirationInSeconds;
    }

    private boolean isTimerRunning() {
        return timer != null && startedAt != null;
    }

    public void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
            startedAt = null;
        }
    }

    public void startTimer(final TimerDelegate timerDelegate) {
        if(timer != null) {
            stopTimer();
        }
        timer = new Timer();
        startedAt = new Date();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerDelegate.onTimerUpdate(new TimerEvent(timeElapsedString()));
                if(timeHasExpired()) {
                    timerDelegate.onTimeExpired(new TimeExpiredEvent(timeElapsedString()));
                    stopTimer();
                }
            }
        }, new Date(), UPDATE_INTERVAL);
    }

    public void expireInSeconds(int endInSeconds) {
        if(isTimerRunning()) {
            long previousExpirationTimeFromStart = expirationInSeconds;
            long newExpirationTimeFromStart = timeElapsedInSeconds() + endInSeconds;
            if(newExpirationTimeFromStart < previousExpirationTimeFromStart) {
                expirationInSeconds = newExpirationTimeFromStart;
            }
        }
    }

    public class TimerEvent {
        private TimerEvent(String timeElapsedString) {
            this.timeElapsedString = timeElapsedString;
        }
        public String timeElapsedString;
    }

    public class TimeExpiredEvent {
        private TimeExpiredEvent(String timeElapsedString){
            this.timeElapsedString = timeElapsedString;
        }
        public String timeElapsedString;
    }

    public interface TimerDelegate {
        void onTimerUpdate(TimerEvent timerEvent);
        void onTimeExpired(TimeExpiredEvent timeExpiredEvent);
    }
}
