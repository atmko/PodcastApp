package com.atmko.podcastapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.podcastapp.di.DaggerApiComponent
import com.atmko.podcastapp.model.ApiResults
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.model.PodcastsService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SearchViewModel: ViewModel() {
    val searchResults:MutableLiveData<List<Podcast>> = MutableLiveData()
    val searchLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val searchLoading: MutableLiveData<Boolean> = MutableLiveData()

    val genreResults:MutableLiveData<List<Podcast>> = MutableLiveData()
    val genreLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val genreLoading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var podcastService: PodcastsService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun search(queryString: String) {
        searchLoading.value = true
        disposable.add(
            podcastService.searchPodcasts(queryString)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<ApiResults>() {
                    override fun onSuccess(results: ApiResults?) {
                        searchResults.value = results?.podcasts
                        searchLoadError.value = false
                        searchLoading.value = false
                    }

                    override fun onError(e: Throwable?) {
                        searchLoadError.value = true
                        searchLoading.value = false
                    }
                })
        )
    }

    fun fetchPodcastsByGenre(genreId: Int) {
        genreLoading.value = true
        disposable.add(
            podcastService.getPodcastsByGenre(genreId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<ApiResults>() {
                    override fun onSuccess(results: ApiResults?) {
                        genreResults.value = results?.podcasts
                        genreLoadError.value = false
                        genreLoading.value = false
                    }

                    override fun onError(e: Throwable?) {
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