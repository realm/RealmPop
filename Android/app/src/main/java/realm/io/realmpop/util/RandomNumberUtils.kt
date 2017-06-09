package realm.io.realmpop.util

import java.util.Random

object RandomNumberUtils {

    private val rand = Random(System.currentTimeMillis())

    fun generateNumbersArray(count: Int, min: Int, max: Int): IntArray {

        val eachNumberRange = Math.round(max.toFloat() / count.toFloat())
        var prevNum = min
        var maxNum = eachNumberRange

        val numbers = IntArray(count)
        for (i in 0..count - 1) {
            if (i == 0) {
                numbers[i] = 0
            } else {
                numbers[i] = generateNumber(prevNum + 1, maxNum)
            }
            prevNum = numbers[i]
            maxNum = prevNum + eachNumberRange
        }
        return numbers
    }

    fun generateNumber(min: Int, max: Int): Int {
        return rand.nextInt(max - min + 1) + min
    }


}
