package realm.io.realmpop.util;

import android.os.Handler;
import android.util.Log;

import realm.io.realmpop.model.Player;

public class AvailableHeartbeat {

    private static final String TAG = AvailableHeartbeat.class.getName();

    private static final int HEARTBEAT_AVAIL_REFRESH_RATE = 20 * 1000; // 20 seconds.
    private boolean isAvailable;
    private Handler repeatSettingAvailable = new Handler();
    private Runnable SetAvailabilityTask = new Runnable() {
        @Override
        public void run() {
            log();
            Player.assignAvailability(isAvailable);
            repeatSettingAvailable.postDelayed(SetAvailabilityTask, HEARTBEAT_AVAIL_REFRESH_RATE);
        }
    };

    private void log() {
        Log.d(TAG, String.format("Set availability:[%b]", isAvailable));
    }

    public void start() {
        isAvailable = true;
        repeatSettingAvailable.post(SetAvailabilityTask);
    }

    public void stop() {
        isAvailable = false;  // in case one final iteration runs, have it set to false on this.
        repeatSettingAvailable.removeCallbacks(SetAvailabilityTask);
        log();
        Player.assignAvailability(isAvailable);  // Run once more to ensure set to false on stop.
    }

}
