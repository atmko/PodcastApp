package com.atmko.skiptoit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.dependencyinjection.DaggerSkipToItApiComponent
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class CommentsViewModel(application: Application): AndroidViewModel(application) {

    @Inject
    lateinit var podcastService: SkipToItService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(getApplication())
    }

    val isCreated: MutableLiveData<Boolean> = MutableLiveData()
    val createError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun createComment(podcastId: String, parentId: String?, episodeId: String, comment: String) {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                isCreated.value = false
                processing.value = true
                disposable.add(
                    podcastService.createComment(podcastId, parentId, episodeId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                isCreated.value = response.isSuccessful
                                createError.value = false
                                processing.value = false
                            }

                            override fun onError(e: Throwable?) {
                                createError.value = true
                                processing.value = false
                            }
                        })
                )
            }
        }
    }

    val episodeComments:MutableLiveData<List<Comment>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getComments(podcastId: String, episodeId: String, page: Int) {
        loading.value = true
        disposable.add(
            podcastService.getComments(podcastId, episodeId, page)
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