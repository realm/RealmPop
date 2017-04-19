package realm.io.realmpop.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import realm.io.realmpop.BuildConfig;

public class SharedPrefsUtils {

    private static final String USER_KEY = "io.realm.realmpop.userKey";
    private static final String LAST_ROS_IP_KEY = "io.realm.realmpop.rosIp";
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

        String idForCurrentUser = sharedPreferences.getString(USER_KEY, null);

        if (idForCurrentUser == null) {
            idForCurrentUser = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(USER_KEY, idForCurrentUser).apply();
        }

        return idForCurrentUser;
    }

    /**
     * Returns the saved ROS Address the app on this device connected to.
     * @return String The IP or URL of the last ROS server, or the default otherwise.
     */
    public String getRosAddress() {
        return sharedPreferences.getString(LAST_ROS_IP_KEY, BuildConfig.DEFAULT_OBJECT_SERVER_IP);
    }

    /**
     * Saves the ROS Address to SharedPreferences.
     */
    public void setRosAddress(String rosAddress) {
        sharedPreferences.edit().putString(LAST_ROS_IP_KEY, rosAddress).apply();
    }


}
