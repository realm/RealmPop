@file:JvmName("RealmUtils") // pretty name for utils class if called from
package realm.io.realmpop.util

import io.realm.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import realm.io.realmpop.model.*

// Realm Extensions
fun Realm.playerDao(synchronous: Boolean = false): PlayerDao = PlayerDao(this, synchronous)
fun Realm.gameDao(synchronous: Boolean = false): GameDao = GameDao(this, synchronous)
fun Realm.sideDao(synchronous: Boolean = false): SideDao = SideDao(this, synchronous)
fun Realm.scoreDao(synchronous: Boolean = false): ScoreDao = ScoreDao(this, synchronous)


// RealmResults Extensions
fun <T:RealmModel> RealmResults<T>.asLiveData() = RealmResultsLiveData<T>(this)

// RealmModel Extensions
fun <T:RealmModel> T.asLiveData() = RealmModelLiveData<T>(this)
val <T:RealmModel> T.isInvalid: Boolean get() = !RealmObject.isValid(this)
