package realm.io.realmpop.util

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager

import java.util.UUID

import realm.io.realmpop.BuildConfig

object SharedPrefsUtils  {

    private val PLAYER_KEY = "io.realm.realmpop.playerKey"
    private val LAST_ROS_HOST_KEY = "io.realm.realmpop.ros.host"
    private val LAST_ROS_USER_KEY = "io.realm.realmpop.ros.user"
    private val LAST_ROS_PASS_KEY = "io.realm.realmpop.ros.pass"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(application: Application) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    }

   /**
     * Get a unique ID for the current player.  This ID will be generated the first time
     * this method is called.  And subsequent times will return the same value even across
     * launches.  Only deleting and reinstalling the app will remove this.
     * @return String Unique UUID for the user of the app on "this" device.
     */
    fun idForCurrentPlayer(): String {

        var idForCurrentUser = sharedPreferences.getString(PLAYER_KEY, null)

        if (idForCurrentUser == null) {
            idForCurrentUser = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(PLAYER_KEY, idForCurrentUser).apply()
        }

        return idForCurrentUser
    }

    var objectServerHost: String
        get() = sharedPreferences.getString(LAST_ROS_HOST_KEY, BuildConfig.DEFAULT_OBJECT_SERVER_IP)
        set(value) = sharedPreferences.edit().putString(LAST_ROS_HOST_KEY, value).apply()

    var popUsername: String
        get() = sharedPreferences.getString(LAST_ROS_USER_KEY, "default@realm")
        set(value) = sharedPreferences.edit().putString(LAST_ROS_USER_KEY, value).apply()

    var popPassword: String
        get() = sharedPreferences.getString(LAST_ROS_PASS_KEY, "password")
        set(value) = sharedPreferences.edit().putString(LAST_ROS_PASS_KEY, value).apply()


}
