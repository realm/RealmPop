package realm.io.realmpop.ui.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.realm.*
import realm.io.realmpop.util.SharedPrefsUtils
import realm.io.realmpop.model.dao.playerDao


class SplashViewModel : ViewModel() {

    enum class State { WAITING_USER, ATTEMPTING_LOGIN, AUTHENTICATED  }

    private var realm: Realm? = null
    private val sharedPrefs = SharedPrefsUtils
    private val serverUrl = { "http://${sharedPrefs.objectServerHost}:9080" }
    private val realmUrl = { "realm://${sharedPrefs.objectServerHost}:9080/~/game" }

    var host = sharedPrefs.objectServerHost
    var username = sharedPrefs.popUsername
    var password = sharedPrefs.popPassword
    var state = MutableLiveData<State>()
    var error = MutableLiveData<String>()

    fun login() {
        Log.d("Tag","Pressed login: ${host}:${username}:${password}")

        state.postValue(State.ATTEMPTING_LOGIN)
        logoutExistingUser()
        updateStoredConnectionParameters()

        val syncCredentials = SyncCredentials.usernamePassword(username, password, false)

        SyncUser.loginAsync(syncCredentials, serverUrl(), object : SyncUser.Callback {
            override fun onSuccess(user: SyncUser) {
                postLogin(user)
            }

            override fun onError(e: ObjectServerError) {

                when(e.errorCode) {
                    ErrorCode.INVALID_CREDENTIALS -> error.postValue("Error: Invalid Credentials...")
                    else -> error.postValue("Error: Could not connect, check host...")
                }
                state.postValue(State.WAITING_USER)

            }
        })
    }

    fun logoutExistingUser() {
         SyncUser.currentUser()?.logout()
    }

    override fun onCleared() {
        realm?.close()
    }

    private fun postLogin(user: SyncUser) {
        setRealmDefaultConfig(user)
        realm = Realm.getDefaultInstance()
        realm!!.playerDao().initializePlayer(onSuccess = { state.postValue(State.AUTHENTICATED) })
    }

    private fun setRealmDefaultConfig(user: SyncUser) {
        Log.d("Tag", "Connecting to Sync Server at : ["  + realmUrl().replace("~", user.identity) + "]");
        Realm.removeDefaultConfiguration()
        Realm.setDefaultConfiguration(SyncConfiguration.Builder(user, realmUrl()).build())
    }

    private fun updateStoredConnectionParameters() {
        sharedPrefs.objectServerHost = host
        sharedPrefs.popUsername = username
        sharedPrefs.popPassword = password
    }

}
