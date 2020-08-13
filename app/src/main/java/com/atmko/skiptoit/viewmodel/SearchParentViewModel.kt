package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastsApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class SearchParentViewModel(private val podcastsApi: PodcastsApi): ViewModel() {
    val searchResults:MutableLiveData<List<Podcast>> = MutableLiveData()
    val searchLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val searchLoading: MutableLiveData<Boolean> = MutableLiveData()

    //state save variables
    var tabPosition: Int = 0

    private val disposable: CompositeDisposable = CompositeDisposable()

    fun search(queryString: String) {
        searchLoading.value = true
        disposable.add(
            podcastsApi.searchPodcasts(queryString)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<ApiResults>() {
                    override fun onSuccess(results: ApiResults) {
                        searchResults.value = results.podcasts
                        searchLoadError.value = false
                        searchLoading.value = false
                    }

                    override fun onError(e: Throwable) {
                        searchLoadError.value = true
                        searchLoading.value = false
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}