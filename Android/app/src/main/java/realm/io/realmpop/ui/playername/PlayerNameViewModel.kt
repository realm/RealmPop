package realm.io.realmpop.ui.playername

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.TextUtils
import io.realm.Realm
import realm.io.realmpop.model.Player
import realm.io.realmpop.model.dao.playerDao

class PlayerNameViewModel : ViewModel() {

    private var realm = Realm.getDefaultInstance()
    private var me: Player? = realm.playerDao().byId()

    enum class State { WAITING_USER, APP_RESTART_NEEDED, BAD_INPUT, COMPLETED }

    var playerNameText = MutableLiveData<String>()
    val state = MutableLiveData<State>()

    init {
        me?.addChangeListener { player: Player, objectChangeSet ->
            if (objectChangeSet.isDeleted || !player.isValid) {
                state.postValue(State.APP_RESTART_NEEDED)
            }
        }
        playerNameText.value = me?.name ?: ""
    }

    override fun onCleared() {
        me?.removeAllChangeListeners()
        realm.close()
    }

    fun onEnterPressed() = if (isPlayerNameValid())
                                state.postValue(State.COMPLETED)
                           else
                                state.postValue(State.BAD_INPUT)


    fun updateName(onSuccess: () -> Unit = {}, onError:(e: Throwable) -> Unit = {}) =
            realm.playerDao().updateName(
                    name = playerNameText.value ?: "",
                    onSuccess = onSuccess,
                    onError = onError)

    private fun isPlayerNameValid() = !TextUtils.isEmpty(playerNameText.value)

}
