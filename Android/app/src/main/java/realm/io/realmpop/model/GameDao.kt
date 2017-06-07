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

}
