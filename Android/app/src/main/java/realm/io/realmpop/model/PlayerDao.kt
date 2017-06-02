package realm.io.realmpop.model

import io.realm.Realm
import org.jetbrains.anko.doAsync
import realm.io.realmpop.util.SharedPrefsUtils

class PlayerDao(val realm: Realm) {

    fun initializePlayer(id:String = SharedPrefsUtils.getInstance().idForCurrentPlayer(),
                         onSuccess: (() -> Unit) = {},
                         onError:   (() -> Unit) = {}) {

        realm.executeTransactionAsync(
                { bgRealm ->
                    val playerId = id
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

    fun byId(realm: Realm, id: String): Player? {
        return realm.where(Player::class.java).equalTo("id", id).findFirst()
    }

}
