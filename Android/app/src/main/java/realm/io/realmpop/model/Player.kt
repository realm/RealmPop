package realm.io.realmpop.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Player : RealmObject() {

    @PrimaryKey
    @Required
    var id: String? = null
    @Required
    var name: String? = null
    var available: Boolean = false
    var challenger: Player? = null
    var currentGame: Game? = null

    fun hasChallengerAssigned() = challenger != null
    fun hasGameAssigned() = currentGame != null



}
