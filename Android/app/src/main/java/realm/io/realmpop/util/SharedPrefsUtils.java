package realm.io.realmpop.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import realm.io.realmpop.BuildConfig;

public class SharedPrefsUtils {

    private static final String PLAYER_KEY = "io.realm.realmpop.playerKey";
    private static final String LAST_ROS_HOST_KEY = "io.realm.realmpop.ros.host";
    private static final String LAST_ROS_USER_KEY = "io.realm.realmpop.ros.user";
    private static final String LAST_ROS_PASS_KEY = "io.realm.realmpop.ros.pass";
    private static SharedPrefsUtils privateInstance;

    private SharedPreferences sharedPreferences;

    public static void init(Application application) {
        if(privateInstance == null) {
            privateInstance = new SharedPrefsUtils(application);
        }
    }

    private SharedPrefsUtils(Application application) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public static SharedPrefsUtils getInstance() {
        return privateInstance;
    }


    /**
     * Get a unique ID for the current player.  This ID will be generated the first time
     * this method is called.  And subsequent times will return the same value even across
     * launches.  Only deleting and reinstalling the app will remove this.
     * @return String Unique UUID for the user of the app on "this" device.
     */
    public String idForCurrentPlayer() {

        String idForCurrentUser = sharedPreferences.getString(PLAYER_KEY, null);

        if (idForCurrentUser == null) {
            idForCurrentUser = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(PLAYER_KEY, idForCurrentUser).apply();
        }

        return idForCurrentUser;
    }

    public String getObjectServerHost() {
        return sharedPreferences.getString(LAST_ROS_HOST_KEY, BuildConfig.DEFAULT_OBJECT_SERVER_IP);
    }

    public void setObjectServerHost(String value) {
        sharedPreferences.edit().putString(LAST_ROS_HOST_KEY, value).apply();
    }

    public String getPopUsername() {
        return sharedPreferences.getString(LAST_ROS_USER_KEY, "");
    }

    public void setPopUsername(String value) {
        sharedPreferences.edit().putString(LAST_ROS_USER_KEY, value).apply();
    }

    public String getPopPassword() {
        return sharedPreferences.getString(LAST_ROS_PASS_KEY, "");
    }

    public void setPopPassword(String value) {
        sharedPreferences.edit().putString(LAST_ROS_PASS_KEY, value).apply();
    }

}
