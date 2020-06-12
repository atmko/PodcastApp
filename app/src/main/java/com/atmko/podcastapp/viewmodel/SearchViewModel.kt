package com.atmko.podcastapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.podcastapp.model.ApiResults
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.model.PodcastsService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class SearchViewModel: ViewModel() {
    val searchResults:MutableLiveData<List<Podcast>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    private val podcastService: PodcastsService = PodcastsService()
    private val disposable: CompositeDisposable = CompositeDisposable()

    fun refresh(genreId: Int) {
        fetchPodcasts(genreId)
    }

    fun fetchPodcasts(genreId: Int) {
        loading.value = true
        disposable.add(
            podcastService.getPodcastsByGenre(genreId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<ApiResults>() {
                    override fun onSuccess(results: ApiResults?) {
                        searchResults.value = results?.podcasts
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable?) {
                        loadError.value = true
                        loading.value = false
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}