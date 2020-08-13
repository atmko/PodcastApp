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

class SearchViewModel(private val podcastsApi: PodcastsApi): ViewModel() {

    val genreResults:MutableLiveData<List<Podcast>> = MutableLiveData()
    val genreLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val genreLoading: MutableLiveData<Boolean> = MutableLiveData()

    //state save variables
    var scrollPosition: Int = 0

    private val disposable: CompositeDisposable = CompositeDisposable()

    fun fetchPodcastsByGenre(genreId: Int) {
        if (genreResults.value != null) {
            return
        }
        genreLoading.value = true
        disposable.add(
            podcastsApi.getPodcastsByGenre(genreId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<ApiResults>() {
                    override fun onSuccess(results: ApiResults) {
                        genreResults.value = results.podcasts
                        genreLoadError.value = false
                        genreLoading.value = false
                    }

                    override fun onError(e: Throwable) {
                        genreLoadError.value = true
                        genreLoading.value = false
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}