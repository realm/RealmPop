package realm.io.realmpop.model

import io.realm.Realm
import realm.io.realmpop.util.SharedPrefsUtils

class PlayerDao(val mRealm: Realm) {

    fun initializePlayer(playerId:String = SharedPrefsUtils.getInstance().idForCurrentPlayer(),
                         onSuccess: (() -> Unit) = {},
                         onError:   (() -> Unit) = {}) {

        mRealm.executeTransactionAsync(
                { bgRealm ->
                    var me = byId(bgRealm, playerId)
                    if (me == null) {
                        me = Player()
                        me.id = playerId
                        me.name = ""
                        me = bgRealm.copyToRealmOrUpdate(me)
                    }
                    me?.isAvailable = false
                    me?.challenger = null
                    me?.currentGame = null
                },
                { onSuccess() },
                { onError() }
        )

    }

    fun updateName(id: String = SharedPrefsUtils.getInstance().idForCurrentPlayer(),
                   name: String,
                   onSuccess: (() -> Unit) = {},
                   onError:   (() -> Unit) = {}) {
        mRealm.executeTransactionAsync(
                { bgRealm ->  byId(bgRealm)?.name = name },
                { onSuccess() },
                { onError() }
        )
    }

    fun byId(realm: Realm = mRealm, id: String = SharedPrefsUtils.getInstance().idForCurrentPlayer()): Player? {
        return realm.where(Player::class.java).equalTo("id", id).findFirst()
    }

}
