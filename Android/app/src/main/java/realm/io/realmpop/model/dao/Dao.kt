package realm.io.realmpop.model.dao

import io.realm.Realm
import io.realm.RealmModel
import realm.io.realmpop.util.SharedPrefsUtils

interface Dao<T: io.realm.RealmModel> {

    val synchronous: Boolean

    fun defPlayerId() = SharedPrefsUtils.idForCurrentPlayer()

    fun execTx(tx: io.realm.Realm.Transaction,
               realm: io.realm.Realm,
               onSuccess: (() -> Unit) = {},
               onError:   ((e: Throwable) -> Unit) = {}) {

        if(synchronous)
            realm.executeTransaction(tx)
        else
            realm.executeTransactionAsync(tx, io.realm.Realm.Transaction.OnSuccess(onSuccess), io.realm.Realm.Transaction.OnError(onError))

    }


}