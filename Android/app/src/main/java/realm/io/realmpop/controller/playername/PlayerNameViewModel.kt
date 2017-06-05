package realm.io.realmpop.controller.playername

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.TextUtils
import io.realm.Realm
import realm.io.realmpop.model.Player
import realm.io.realmpop.util.playerDao
import rx.Observable
import rx.Subscription

class PlayerNameViewModel : ViewModel() {

    private var realm = Realm.getDefaultInstance()
    private var me: Player? = realm.playerDao().byId()
    private var playerNameTextSubscripton: Subscription? = null

    enum class State { WAITING_USER, APP_RESTART_NEEDED, BAD_INPUT, COMPLETED }

    var playerNameText = me?.name ?: ""
    val state = MutableLiveData<State>()

    init {
        me?.addChangeListener { player: Player, objectChangeSet ->
            if (objectChangeSet.isDeleted || !player.isValid) {
                state.postValue(State.APP_RESTART_NEEDED)
            }
        }
        playerNameTextSubscripton = Observable.just(playerNameText).subscribe{ updateName() }
    }

    override fun onCleared() {
        me?.removeAllChangeListeners()
        playerNameTextSubscripton?.unsubscribe()
        realm.close()
    }

    fun onEnterPressed() = if (isPlayerNameValid())
                                state.postValue(State.COMPLETED)
                           else
                                state.postValue(State.BAD_INPUT)

    fun updateName(onSuccess: () -> Unit = {}, onFailure:() -> Unit = {}) =
            realm.playerDao().updateName(name=playerNameText)


    private fun isPlayerNameValid() = !TextUtils.isEmpty(playerNameText)

}
