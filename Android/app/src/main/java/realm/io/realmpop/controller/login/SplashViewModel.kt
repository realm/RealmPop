package realm.io.realmpop.controller.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.realm.*
import realm.io.realmpop.util.SharedPrefsUtils
import realm.io.realmpop.util.playerDao

enum class LoginState {
    WAITING_USER, ATTEMPTING_LOGIN, AUTHENTICATED
}

class SplashViewModel : ViewModel() {

    private lateinit var realm: Realm
    private val sharedPrefs = SharedPrefsUtils.getInstance()
    private val serverUrl: String
        get() = "http://${sharedPrefs.objectServerHost}:9080"
    private val realmUrl: String
        get() = "realm://${sharedPrefs.objectServerHost}:9080/~/game"

    var host = sharedPrefs.objectServerHost
    var username = sharedPrefs.popUsername
    var password = sharedPrefs.popPassword
    var state = MutableLiveData<LoginState>()
    var error = MutableLiveData<String>()

    init {
        state.value = LoginState.WAITING_USER
        error.value = ""
    }

    private fun updateStoredConnectionParameters() {
        sharedPrefs.objectServerHost = host
        sharedPrefs.popUsername = username
        sharedPrefs.popPassword = password
    }

    fun login() {
        Log.d("Tag","Pressed login: ${host}:${username}:${password}")

        state.postValue(LoginState.ATTEMPTING_LOGIN)
        logoutExistingUser()
        updateStoredConnectionParameters()

        val syncCredentials = SyncCredentials.usernamePassword(username, password, false)

        SyncUser.loginAsync(syncCredentials, serverUrl, object : SyncUser.Callback {
            override fun onSuccess(user: SyncUser) {
                postLogin(user)
            }

            override fun onError(e: ObjectServerError) {

                when(e.errorCode) {
                    ErrorCode.INVALID_CREDENTIALS -> error.postValue("Error: Invalid Credentials...")
                    else -> error.postValue("Error: Could not connect, check host...")
                }
                state.postValue(LoginState.WAITING_USER)

            }
        })
    }

    private fun logoutExistingUser() {
         SyncUser.currentUser()?.logout()
    }

    private fun postLogin(user: SyncUser) {
        setRealmDefaultConfig(user)
        realm = Realm.getDefaultInstance()
        realm.playerDao().initializePlayer(onSuccess = { state.postValue(LoginState.AUTHENTICATED) })
    }

    private fun setRealmDefaultConfig(user: SyncUser) {
        Log.d("Tag", "Connecting to Sync Server at : ["  + realmUrl.replace("~", user.getIdentity()) + "]");
        Realm.removeDefaultConfiguration()
        Realm.setDefaultConfiguration(SyncConfiguration.Builder(user, realmUrl).build())
    }

    override fun onCleared() {
        realm.close()
    }

}
