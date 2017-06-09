@file:JvmName("RealmUtils")
package realm.io.realmpop.model.dao

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import realm.io.realmpop.model.dao.GameDao
import realm.io.realmpop.model.dao.PlayerDao

// Realm Extensions
fun Realm.playerDao(synchronous: Boolean = false): PlayerDao = PlayerDao(this, synchronous)
fun Realm.gameDao(synchronous: Boolean = false): GameDao = GameDao(this, synchronous)

// RealmModel Extensions
val <T: RealmModel> T.isInvalid: Boolean get() = !RealmObject.isValid(this)
