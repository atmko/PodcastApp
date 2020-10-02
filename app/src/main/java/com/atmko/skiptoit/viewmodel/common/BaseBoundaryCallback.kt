package com.atmko.skiptoit.viewmodel.common

import androidx.paging.PagedList
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class BaseBoundaryCallback<Type> : PagedList.BoundaryCallback<Type>() {

    interface Listener {
        fun onPageLoading()
        fun onPageLoad()
        fun onPageLoadFailed()
    }

    // thread-safe set of listeners
    private val mListeners: MutableSet<Listener> =
        Collections.newSetFromMap(
            ConcurrentHashMap<Listener, Boolean>(1)
        )

    fun registerListener(listener: Listener) {
        mListeners.add(listener)
    }

    fun unregisterListener(listener: Listener?) {
        mListeners.remove(listener)
    }

    private fun getListeners(): Set<Listener> {
        return Collections.unmodifiableSet(mListeners)
    }

    protected fun notifyPageLoading() {
        for (listener in getListeners()) {
            listener.onPageLoading()
        }
    }

    protected fun notifyOnPageLoad() {
        for (listener in getListeners()) {
            listener.onPageLoad()
        }
    }

    protected fun notifyOnPageLoadFailed() {
        for (listener in getListeners()) {
            listener.onPageLoadFailed()
        }
    }
}