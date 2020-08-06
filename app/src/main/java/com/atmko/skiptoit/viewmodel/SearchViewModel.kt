package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.dependencyinjection.application.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastsApi
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

    //state save variables
    var tabPosition: Int = 0
    var scrollPosition: Int = 0

    @Inject
    lateinit var podcastService: PodcastsApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
    }

    fun search(queryString: String) {
        searchLoading.value = true
        disposable.add(
            podcastService.searchPodcasts(queryString)
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

    fun fetchPodcastsByGenre(genreId: Int) {
        genreLoading.value = true
        disposable.add(
            podcastService.getPodcastsByGenre(genreId)
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