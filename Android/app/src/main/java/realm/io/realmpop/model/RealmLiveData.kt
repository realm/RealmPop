package realm.io.realmpop.model

import android.arch.lifecycle.LiveData

import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults

/**
 * Class connecting the Realm lifecycle to that of LiveData objects.
 */
class LiveRealmData<T : RealmModel>(private val results: RealmResults<T>) : LiveData<RealmResults<T>>() {
    private val listener = RealmChangeListener<RealmResults<T>> { results -> value = results }

    override fun onActive() {
        results.addChangeListener(listener)
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }

}
