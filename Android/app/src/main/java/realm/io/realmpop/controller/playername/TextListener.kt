package realm.io.realmpop.controller.playername

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.widget.EditText
import com.jakewharton.rxbinding.widget.RxTextView
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent
import rx.Subscription

class TextListener(val context: Context,
                   val lifecycle: Lifecycle,
                   val textField: EditText,
                   val onChange: ((textChangedEvent: TextViewTextChangeEvent) -> Unit)) : LifecycleObserver {

    var subscription: Subscription? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        RxTextView.textChangeEvents(textField).subscribe(onChange);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        subscription?.unsubscribe()
    }

}