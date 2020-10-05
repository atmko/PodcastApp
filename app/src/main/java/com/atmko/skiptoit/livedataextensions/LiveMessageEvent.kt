//source: https://medium.com/@erik_90880/sending-events-from-an-mvvm-view-model-with-kotlin-19fdce61dcb9
package com.atmko.skiptoit.livedataextensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class LiveMessageEvent<T> : SingleLiveEvent<(T.() -> Unit)?>() {

    fun setEventReceiver(owner: LifecycleOwner, receiver: T) {
        observe(owner, Observer { event ->
            if ( event != null ) {
                receiver.event()
            }
        })
    }

    fun sendEvent(event: (T.() -> Unit)?) {
        value = event
    }
}