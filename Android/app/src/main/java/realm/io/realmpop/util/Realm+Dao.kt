@file:JvmName("RealmUtils") // pretty name for utils class if called from
package realm.io.realmpop.util

import io.realm.*
import realm.io.realmpop.model.GameDao
import realm.io.realmpop.model.PlayerDao
import realm.io.realmpop.model.RealmModelLiveData
import realm.io.realmpop.model.RealmResultsLiveData

// Realm Extensions
fun Realm.playerDao(synchronous: Boolean = false): PlayerDao = PlayerDao(this, synchronous)
fun Realm.gameDao(synchronous: Boolean = false): GameDao = GameDao(this, synchronous)

// RealmResults Extensions
fun <T:RealmModel> RealmResults<T>.asLiveData() = RealmResultsLiveData<T>(this)

// RealmModel Extensions
fun <T:RealmModel> T.asLiveData() = RealmModelLiveData<T>(this)
val <T:RealmModel> T.isInvalid: Boolean get() = !RealmObject.isValid(this)
