package realm.io.realmpop.model

import io.realm.Realm
import io.realm.RealmModel
import realm.io.realmpop.util.SharedPrefsUtils

interface Dao<T: RealmModel> {

    val synchronous: Boolean

    fun defPlayerId() = SharedPrefsUtils.getInstance().idForCurrentPlayer()

    fun execTx(tx: Realm.Transaction,
               realm: Realm,
               onSuccess: (() -> Unit) = {},
               onError:   ((e: Throwable) -> Unit) = {}) {

        if(synchronous)
            realm.executeTransaction(tx)
        else
            realm.executeTransactionAsync(tx, Realm.Transaction.OnSuccess(onSuccess), Realm.Transaction.OnError(onError))

    }


}