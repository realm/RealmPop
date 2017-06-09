package realm.io.realmpop.model

import io.realm.Realm

class ScoreDao(val mRealm: Realm, override val synchronous: Boolean) : Dao<Score> {

    fun addNewScore(winnerName: String, finishTime: Double) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransactionAsync { bgRealm ->
                val score = Score()
                score.name = winnerName
                score.time = finishTime
                bgRealm.copyToRealm(score)
            }
        }
    }

}
