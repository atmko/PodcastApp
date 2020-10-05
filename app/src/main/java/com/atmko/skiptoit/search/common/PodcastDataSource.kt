package com.atmko.skiptoit.search.common

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast

abstract class PodcastDataSource : PageKeyedDataSource<Int, Podcast>() {

    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    val startingPage = 1

    fun getNextPage(apiResults: ApiResults, currentPage: Int): Int? {
        return if (apiResults.hasNext) {
            currentPage + 1
        } else {
            null
        }
    }
}