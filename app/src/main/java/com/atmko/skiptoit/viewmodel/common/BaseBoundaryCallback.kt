package com.atmko.skiptoit.viewmodel.common

import androidx.paging.PagedList
import com.atmko.skiptoit.util.AppExecutors
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

    protected fun getListeners(): Set<Listener> {
        return Collections.unmodifiableSet(mListeners)
    }

    fun notifyOnPageLoad(listener: Listener) {
        AppExecutors.getInstance().mainThread().execute {
            listener.onPageLoad()
        }
    }

    fun notifyOnPageLoadFailed(listener: Listener) {
        AppExecutors.getInstance().mainThread().execute {
            listener.onPageLoadFailed()
        }
    }
}