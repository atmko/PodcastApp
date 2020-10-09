package com.atmko.skiptoit.search.common

import androidx.paging.PageKeyedDataSource
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class PodcastDataSource : PageKeyedDataSource<Int, Podcast>() {

    interface Listener {
        fun onPageLoading()
        fun onPageLoad()
        fun onPageLoadFailed()
    }

    val startingPage = 1

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

    fun getNextPage(apiResults: ApiResults, currentPage: Int): Int? {
        return if (apiResults.hasNext) {
            currentPage + 1
        } else {
            null
        }
    }
}