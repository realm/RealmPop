package realm.io.realmpop.controller.game

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import io.realm.ObjectChangeSet
import io.realm.Realm
import io.realm.RealmObject
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread
import realm.io.realmpop.R
import realm.io.realmpop.RealmPopApplication
import realm.io.realmpop.model.Game
import realm.io.realmpop.model.Player
import realm.io.realmpop.model.Side
import realm.io.realmpop.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Executors.callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class GameViewModel(application: Application) : AndroidViewModel(application), AnkoLogger, GameTimer.TimerDelegate {

    enum class State { IN_PROGRESS, GAME_FINISHED, APP_RESTART_NEEDED }

    private val realm = Realm.getDefaultInstance()
    private val playerDao = realm.playerDao()
    private val gameDao = realm.gameDao()
    private val sideDao = realm.sideDao()
    private val scoreDao = realm.scoreDao()

    private val me = playerDao.byId()!!
    private val challenge = me.currentGame
    private val mySide = if (challenge.player1.playerId == me.id) challenge.player1 else challenge.player2
    private var otherSide = if (challenge.player1.playerId == me.id) challenge.player2 else challenge.player1
    private var timer = GameTimer(PopUtils.GAME_LENGTH_SECONDS)

    val numbers = challenge.numberArray
    val state = MutableLiveData<State>()
    var message = ObservableField<String>()
    var clockDisplay = ObservableField<String>()
    var leftPlayerLabel = ObservableField<String>()
    var rightPlayerLabel = ObservableField<String>()

    init {

        state.value = State.IN_PROGRESS
        clockDisplay.set("0.0")
        leftPlayerLabel.set(challenge.player1.name + " : " + challenge.player1.left)
        rightPlayerLabel.set(challenge.player2.name + " : " + challenge.player2.left)


        me.addChangeListener {  player: Player, changeSet ->
            if(changeSet.isDeleted || player.isInvalid) { moveTo(State.APP_RESTART_NEEDED) }
            if(!me.hasGameAssigned()) { moveTo(State.GAME_FINISHED) }
        }

        challenge.addChangeListener { game: Game, changeSet ->
            if (changeSet.isDeleted || game.isInvalid) { moveTo(State.GAME_FINISHED) }
        }

        val onSideChange = { side: Side, changeSet: ObjectChangeSet ->
            if (changeSet.isDeleted || !side.isValid) { moveTo(State.GAME_FINISHED) }
            if (challenge.isGameOver) {
                mySide.removeAllChangeListeners()
                otherSide.removeAllChangeListeners()
            }
            update()
        }
        mySide.addChangeListener(onSideChange)
        otherSide.addChangeListener(onSideChange)
        timer.startTimer(this)
    }

    override fun onCleared() {
        timer.stopTimer()
        listOf(me, challenge, mySide, otherSide).forEach(RealmObject::removeAllChangeListeners)
    }

    override fun onTimerUpdate(timerEvent: GameTimer.TimerEvent) {
        runOnUiThread { clockDisplay.set(timerEvent.timeElapsedString) }
    }

    override fun onTimeExpired(timeExpiredEvent: GameTimer.TimeExpiredEvent) {
        runOnUiThread {
           clockDisplay.set(timeExpiredEvent.timeElapsedString)
           if(message.get()?.isEmpty() ?: true) {
               message.set(getApplication<RealmPopApplication>().getString(R.string.game_outcome_message_timeout))
           }
           if(!challenge.isGameOver) {
               gameDao.failUnfinishedSides(challenge)
           }
        }
    }

    fun Any.runOnUiThread(block: ()->Unit) = doAsync{ uiThread { block() } }

    fun onBubbleTap(numberTapped: Long) {

        // Just to make sure, if there are none left to tap, exit.
        if (mySide.left <= 0) {
            return
        }

        val numberThatShouldBeTapped = challenge.numberArray[mySide.left.toInt() - 1].toLong()
        val mySidePlayerId = mySide.playerId

        if (numberThatShouldBeTapped == numberTapped) {
            sideDao.decrementBubbleCount(mySidePlayerId)

        } else {

            realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                val currentGame = Player.byId(bgRealm, mySidePlayerId).currentGame
                val mySide = currentGame.sideWithPlayerId(mySidePlayerId)
                mySide.isFailed = true
            }, Realm.Transaction.OnSuccess {
                message.set(getApplication<RealmPopApplication>().getString(R.string.game_outcome_message_popoutoforder, numberTapped, numberThatShouldBeTapped))
            })
        }
    }

    fun exitGameAfterDelay(delay: Int) {
        doIn(delay) {
            playerDao.assignAvailability(true, mySide.playerId)
            playerDao.assignAvailability(true, otherSide.playerId)
        }
    }

    fun doIn(millis: Int, action: ()->Any) {
        doAsync {
            Thread.sleep(millis.toLong())
            uiThread {
                action()
            }
        }
    }

    private fun moveTo(nextState: GameViewModel.State) {
        info("Moving From: [${state.value?.name ?: "UNDEFINED"} -> ${nextState.name}]")
        state.postValue(nextState)
    }


    private fun update() {

        if (!me.hasGameAssigned()) { return }

        val mySidePlayerId = mySide!!.playerId
        val otherSidePlayerId = otherSide!!.playerId

        realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
            val currentGame = Player.byId(bgRealm, mySidePlayerId).currentGame
            val mySide = currentGame.sideWithPlayerId(mySidePlayerId)
            val otherSide = currentGame.sideWithPlayerId(otherSidePlayerId)

            if (mySide.left == 0L) {

                // MySide finished but time hasn't been recorded yet, so let's do that.
                if (mySide.time == 0.0) {
                    mySide.time = java.lang.Double.valueOf(clockDisplay.get())
                    timer.expireInSeconds(20)
                }

                // Both side times have been recorded, the game is over.
                if (otherSide.time > 0 && mySide.time > 0) {
                    if (otherSide.time < mySide.time) {
                        mySide.isFailed = true
                    } else {
                        otherSide.isFailed = true
                    }
                }
            }
        }, Realm.Transaction.OnSuccess {

            leftPlayerLabel.set(challenge.player1.name + " : " + challenge.player1.left)
            rightPlayerLabel.set(challenge.player2.name + " : " + challenge.player2.left)

            if (mySide.time > 0) {
                val finishTimeMsg = getApplication<RealmPopApplication>().getString(R.string.game_outcome_message_finishtime, mySide.time.toString())
                message.set(finishTimeMsg)
            }

            if (challenge.isGameOver) {

                timer.stopTimer()

                if (mySide.isFailed) {
                    if (message.get()?.isEmpty() ?: true) {
                        message.set(getApplication<RealmPopApplication>().getString(R.string.game_outcome_message_lose))
                    }

                } else if (otherSide.isFailed) {
                    message.set(getApplication<RealmPopApplication>().getString(R.string.game_outcome_message_win))
                    scoreDao.addNewScore(mySide.name, mySide.time)
                }

                exitGameAfterDelay(5000)
            }

        })
    }



}
