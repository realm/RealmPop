package realm.io.realmpop.model.dao

import io.realm.Realm
import realm.io.realmpop.model.Side
import realm.io.realmpop.util.RandomNumberUtils.generateNumbersArray
import realm.io.realmpop.model.*
import realm.io.realmpop.util.BUBBLE_COUNT
import realm.io.realmpop.util.BUBBLE_VALUE_MAX

class GameDao(val mRealm: io.realm.Realm, override val synchronous: Boolean) : Dao<Game> {

    fun startNewGame(meId: String, challengerId: String) {

        val tx = io.realm.Realm.Transaction { r ->
            val playerDao = r.playerDao(synchronous=true)
            val me =  playerDao.byId(r, meId)
            val challenger = playerDao.byId(r, challengerId)

            if (me != null && challenger != null) {
                val numbers = generateNumbersArray(BUBBLE_COUNT, 1, BUBBLE_VALUE_MAX)
                var game = Game(me, challenger, numbers)
                game = r.copyToRealm(game)
                me.currentGame = game
                challenger.currentGame = game
            }
        }

        execTx(tx, mRealm)

    }

    fun failUnfinishedSides(game: Game) {

        val p1id = game.player1?.playerId!!
        val p2id = game.player2?.playerId!!

        val tx = io.realm.Realm.Transaction { r ->
            val currentGame =  r.playerDao(synchronous=true).byId(r, p1id)?.currentGame!!
            val p1Side = currentGame.sideWithPlayerId(p1id)!!
            val p2Side = currentGame.sideWithPlayerId(p2id)!!

            if (p1Side.time == 0.0) {
                p1Side.failed = true
            }
            if (p2Side.time == 0.0) {
                p2Side.failed = true
            }
        }

        execTx(tx, mRealm)

    }

    fun failSide(sidesPlayerId: String,
                 realm: io.realm.Realm = mRealm,
                 onSuccess: (() -> Unit) = {},
                 onError: ((e: Throwable) -> Unit) = {}) {

        val tx = io.realm.Realm.Transaction { r -> sideFor(sidesPlayerId, r)?.failed = true }

        execTx(tx, realm, onSuccess, onError)
    }


    fun addNewScore(winnerName: String, finishTime: Double) {
        io.realm.Realm.getDefaultInstance().use { realm ->
            realm.executeTransactionAsync { bgRealm ->
                val score = Score()
                score.name = winnerName
                score.time = finishTime
                bgRealm.copyToRealm(score)
            }
        }
    }

    fun decrementBubbleCount(sidesPlayerId: String) {
        val tx = io.realm.Realm.Transaction { r ->
            val sideToDecrement = sideFor(sidesPlayerId, r)
            if (sideToDecrement != null) {
                val newVal = sideToDecrement.left - 1
                sideToDecrement.left = newVal
            }
        }

        execTx(tx, mRealm)
    }

    private fun sideFor(sidesPlayerId: String, realm: Realm): Side? {
        return realm.playerDao(synchronous=true)
                .byId(id=sidesPlayerId)
                ?.currentGame
                ?.sideWithPlayerId(sidesPlayerId)
    }

}
