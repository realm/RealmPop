package realm.io.realmpop.model

import io.realm.RealmObject
import io.realm.annotations.Required

open class Score : RealmObject() {
    @Required
    var name: String? = null
    var time: Double = 0.toDouble()
}
