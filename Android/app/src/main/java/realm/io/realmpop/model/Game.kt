package realm.io.realmpop.model

import io.realm.RealmObject
import io.realm.annotations.Required
import java.util.*

open class Game : RealmObject {

    var player1: Side? = null
    var player2: Side? = null
    @Required
    var numbers: String? = null

    constructor() {}
    constructor(me: Player, challenger: Player, numberArray: IntArray) {
        player1 = Side(me, numberArray.size)
        player2 = Side(challenger, numberArray.size)
        numbers = numberArrayToString(numberArray)
    }

    val numberArray: IntArray
        get() {
            if (numbers == null) {
                return intArrayOf()
            } else {
                val numberStrArr = numbers!!
                        .split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val numArr = IntArray(numberStrArr.size)
                for (i in numArr.indices) {
                    numArr[i] = Integer.valueOf(numberStrArr[i])!!
                }
                return numArr
            }
        }

    val isGameOver: Boolean
        get() = player1!!.failed || player2!!.failed


    fun sideWithPlayerId(playerId: String) : Side? {
        return when {
            player1?.playerId.equals(playerId) -> player1
            player2?.playerId.equals(playerId) -> player2
            else -> null
        }
    }

    private fun numberArrayToString(numArray: IntArray): String {
        val numStr = Arrays.toString(numArray)
        return numStr.replace("\\[|\\]|\\s".toRegex(), "")
    }

}
