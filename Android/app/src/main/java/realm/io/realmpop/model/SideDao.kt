package realm.io.realmpop.model

import io.realm.Realm


class SideDao(val mRealm: Realm, override val synchronous: Boolean) : Dao<Side> {

    fun decrementBubbleCount(sidesPlayerId: String) {
        val tx = Realm.Transaction { r ->
            val currentGame = r.where(Player::class.java).equalTo("id", sidesPlayerId).findFirst()?.currentGame
            val mySide = currentGame?.sideWithPlayerId(sidesPlayerId)
            if(mySide != null) {
                val newVal = mySide.left - 1
                mySide.left = newVal
            }
        }

        execTx(tx, mRealm)
    }

}