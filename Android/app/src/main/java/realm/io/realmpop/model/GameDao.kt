package realm.io.realmpop.model

import io.realm.Realm
import realm.io.realmpop.util.PopUtils
import realm.io.realmpop.util.RandomNumberUtils.generateNumbersArray

class GameDao(val mRealm: Realm, override val synchronous: Boolean) : Dao<Game> {

    fun startNewGame(meId: String, challengerId: String) {

        val tx = Realm.Transaction { r ->
            val me = Player.byId(r, meId)
            val challenger = Player.byId(r, challengerId)

            if (me != null && challenger != null) {
                val numbers = generateNumbersArray(PopUtils.BUBBLE_COUNT, 1, PopUtils.BUBBLE_VALUE_MAX)
                var game = Game(me, challenger, numbers)
                game = r.copyToRealm(game)
                me.currentGame = game
                challenger.currentGame = game
            }
        }

        execTx(tx, mRealm)

    }

    fun failUnfinishedSides(game: Game) {

        val p1id = game.player1.playerId
        val p2id = game.player2.playerId

        val tx = Realm.Transaction { r ->
            val currentGame = Player.byId(r, p1id).currentGame
            val p1Side = currentGame.sideWithPlayerId(p1id)
            val p2Side = currentGame.sideWithPlayerId(p2id)

            if (p1Side.time == 0.0) {
                p1Side.isFailed = true
            }
            if (p2Side.time == 0.0) {
                p2Side.isFailed = true
            }
        }

        execTx(tx, mRealm)

    }



}
