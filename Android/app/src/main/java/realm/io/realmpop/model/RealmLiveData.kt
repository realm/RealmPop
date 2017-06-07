package realm.io.realmpop.model

import android.arch.lifecycle.LiveData
import io.realm.*

/**
 * Classes connecting the Realm lifecycle to that of LiveData objects.
 */

class RealmResultsLiveData<T : RealmModel>(private val results: RealmResults<T>) : LiveData<RealmResults<T>>() {

    private val listener = RealmChangeListener<RealmResults<T>> { results -> value = results }

    override fun onActive() {
        results.addChangeListener(listener)
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }

}

class RealmModelLiveData<T : RealmModel>(private val mRealmModel: T) : LiveData<ObjectChangedEvent<T>>() {

    private val listener = RealmObjectChangeListener<T> { results, changeSet ->
        value = ObjectChangedEvent<T>(results, changeSet)
    }

    override fun onActive() {
        RealmObject.addChangeListener(mRealmModel, listener)
    }

    override fun onInactive() {
        RealmObject.removeChangeListener(mRealmModel, listener)
    }

}

class ObjectChangedEvent<out T: RealmModel>(val result: T, val changeSet: ObjectChangeSet)