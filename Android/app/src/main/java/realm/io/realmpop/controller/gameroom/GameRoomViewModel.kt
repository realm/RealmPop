package realm.io.realmpop.controller.gameroom

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import realm.io.realmpop.model.Player
import realm.io.realmpop.util.SharedPrefsUtils
import realm.io.realmpop.util.gameDao
import realm.io.realmpop.util.isInvalid
import realm.io.realmpop.util.playerDao

class GameRoomViewModel : ViewModel(), AnkoLogger {

    enum class State { VIEWING, CHALLENGED, CHALLENGING, STARTING_GAME, APP_RESTART_NEEDED }

    private val realm = Realm.getDefaultInstance()
    private val playerDao = realm.playerDao()
    private val gameDao = realm.gameDao()
    private var me = playerDao.byId()
    private var currentChallegee: Player? = null
        set(value) {
            field?.removeAllChangeListeners()
            field = null
            value?.addChangeListener { theChallenged: Player ->
                val stateComingFrom = state.value
                val theChallengerIsMe = theChallenged.challenger?.id == me?.id
                val theChallengerIsNotMe = !theChallengerIsMe

                when (stateComingFrom) {
                    State.VIEWING -> {
                        when {
                            theChallengerIsMe -> moveTo(State.CHALLENGING)
                        }
                    }
                    State.CHALLENGING -> {
                        when {
                            theChallengerIsNotMe -> moveTo(State.VIEWING)
                        }
                    }
                    else -> Unit
                }
            }
            field = value
        }

    val challengerName get() = currentChallegee?.name ?: ""
    val otherPlayers = playerDao.otherPlayers()
    val state = MutableLiveData<State>()

    fun moveTo(nextState: State) {
        info("Moving From: [${state.value?.name ?: "UNDEFINED"} -> ${nextState.name}]")
        state.postValue(nextState)
    }

    init {
        state.value = State.VIEWING // Initial State

        me?.addChangeListener { me: Player, changeSet ->

            if (changeSet.isDeleted || me.isInvalid) { moveTo(State.APP_RESTART_NEEDED) }

            val stateComingFrom = state.value
            when (stateComingFrom) {
                State.CHALLENGED -> {
                    when {
                        me.hasGameAssigned() -> moveTo(State.STARTING_GAME) // Adv to game
                        !me.hasChallengerAssigned() -> moveTo(State.VIEWING) // Back to view
                    }
                }
                State.VIEWING -> {
                    when {
                        me.hasChallengerAssigned() -> moveTo(State.CHALLENGED) // Adv to challenged
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        me?.removeAllChangeListeners()
        currentChallegee?.removeAllChangeListeners()
        realm.close()
    }

    fun challengePlayer(challengerId: String) {
        currentChallegee = playerDao.byId(id=challengerId)
        playerDao.challengePlayer(challengerId)
    }

    fun acceptChallenge() {
        if(currentChallegee != null) {
            gameDao.startNewGame(
                    SharedPrefsUtils.getInstance().idForCurrentPlayer(),
                    currentChallegee!!.id)
        }
    }

    fun declineChallenge() {
        playerDao.assignAvailability(true)
    }
}