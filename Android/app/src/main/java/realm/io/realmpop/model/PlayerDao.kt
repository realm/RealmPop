package realm.io.realmpop.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import realm.io.realmpop.util.SharedPrefsUtils

class PlayerDao(val mRealm: Realm, override val synchronous: Boolean) : Dao<Player> {

    fun initializePlayer(playerId:String = defPlayerId(),
                         onSuccess: (() -> Unit) = {},
                         onError:   ((e: Throwable) -> Unit) = {}) {

        val tx = Realm.Transaction { realm ->
            var me = byId(realm, playerId)
            if (me == null) {
                me = Player()
                me.id = playerId
                me.name = ""
                me = realm.copyToRealmOrUpdate(me)
            }
            me?.isAvailable = false
            me?.challenger = null
            me?.currentGame = null
        }

        execTx(tx, mRealm, onSuccess, onError)

    }

    fun updateName(id: String = defPlayerId(),
                   name: String,
                   onSuccess: (() -> Unit) = {},
                   onError:   ((e: Throwable) -> Unit) = {}) {

        val tx = Realm.Transaction { realm ->  byId(realm, id)?.name = name }

        execTx(tx, mRealm, onSuccess, onError)
    }

    fun assignAvailability(isAvailable: Boolean,
                           playerId: String = defPlayerId(),
                           realm: Realm = mRealm,
                           onSuccess: (() -> Unit) = {},
                           onError:   ((e: Throwable) -> Unit) = {}) {

        val tx = Realm.Transaction { realm: Realm ->
            val me = Player.byId(realm, playerId)
            me.isAvailable = isAvailable
            if (isAvailable) {
                me.challenger = null
                me.currentGame = null
            }
        }

        execTx(tx, realm, onSuccess, onError)
    }

    fun challengePlayer(playerId: String) {

        val tx = Realm.Transaction { realm ->
            val me = byId(realm)
            val theChallenged = byId(realm, playerId)
            if (me != null && theChallenged != null && theChallenged.challenger == null) { //don't want to double challenge.
                theChallenged.challenger = me
            }
        }

        execTx(tx, mRealm)
    }

    fun byId(realm: Realm = mRealm, id: String = defPlayerId()): Player? {
        return realm.where(Player::class.java).equalTo("id", id).findFirst()
    }

    fun otherPlayers(playerId: String = defPlayerId()): RealmResults<Player> =
        mRealm.where(Player::class.java)
             .notEqualTo("id", playerId)
             .findAllSortedAsync("available", Sort.DESCENDING)
}

