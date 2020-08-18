package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.viewmodel.common.CommentsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class ParentCommentsViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient
) : CommentsViewModel(skipToItApi, googleSignInClient) {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getComments(episodeId: String, page: Int) {
        if (retrievedComments.value != null) {
            return
        }
        loading.value = true
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.getCommentsAuthenticated(episodeId, page, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<ArrayList<Comment>>() {
                            override fun onSuccess(comments: ArrayList<Comment>) {
                                retrievedComments.value = comments
                                loadError.value = false
                                loading.value = false
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                            }
                        })
                )
            }
        }.addOnFailureListener {
            disposable.add(
                skipToItApi.getCommentsUnauthenticated(episodeId, page)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<ArrayList<Comment>>() {
                        override fun onSuccess(comments: ArrayList<Comment>) {
                            retrievedComments.value = comments
                            loadError.value = false
                            loading.value = false
                        }

                        override fun onError(e: Throwable) {
                            loadError.value = true
                            loading.value = false
                        }
                    })
            )
        }
    }

    override fun onCleared() {
        disposable.clear()
    }
}