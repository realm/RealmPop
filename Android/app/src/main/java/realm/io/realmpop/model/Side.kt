package realm.io.realmpop.model

import io.realm.RealmObject
import io.realm.annotations.Required

open class Side : RealmObject {

    @Required
    var playerId: String? = null

    @Required
    var name: String? = null
    var left: Long = 0
    var time: Double = 0.toDouble()
    var failed: Boolean = false

    constructor() {}

    constructor(representedPlayer: Player, initialBubbleCount: Int) {
        playerId = representedPlayer.id
        name = representedPlayer.name
        left = initialBubbleCount.toLong()
    }
}
