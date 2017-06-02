@file:JvmName("RealmUtils") // pretty name for utils class if called from
package realm.io.realmpop.util

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.SyncUser
import realm.io.realmpop.model.PlayerDao
import realm.io.realmpop.model.LiveRealmData

// Realm Extensions
fun Realm.playerDao(): PlayerDao = PlayerDao(this)

// RealmResults Extensions
fun <T:RealmModel> RealmResults<T>.asLiveData() = LiveRealmData<T>(this)
