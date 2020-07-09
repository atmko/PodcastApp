package com.atmko.podcastapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.podcastapp.dependencyinjection.DaggerSkipToItApiComponent
import com.atmko.podcastapp.model.Comment
import com.atmko.podcastapp.model.SkipToItService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CommentsViewModel(application: Application): AndroidViewModel(application) {
    val episodeComments:MutableLiveData<List<Comment>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var podcastService: SkipToItService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    fun getComments(podcastId: String, page: Int) {
        loading.value = true
        disposable.add(
            podcastService.getComments(podcastId, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<Comment>>() {
                    override fun onSuccess(comments: List<Comment>?) {
                        episodeComments.value = comments
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