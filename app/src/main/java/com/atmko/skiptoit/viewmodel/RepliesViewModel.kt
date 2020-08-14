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

class RepliesViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient
) : CommentsViewModel(skipToItApi, googleSignInClient) {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val commentReplies: MutableLiveData<List<Comment>> = MutableLiveData()
    val repliesLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val repliesLoading: MutableLiveData<Boolean> = MutableLiveData()

    //network call to get comment's replies
    fun getReplies(commentId: String, page: Int) {
        repliesLoading.value = true
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.getRepliesAuthenticated(commentId, page, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<Comment>>() {
                            override fun onSuccess(replies: List<Comment>) {
                                commentReplies.value = replies
                                repliesLoadError.value = false
                                repliesLoading.value = false
                            }

                            override fun onError(e: Throwable) {
                                repliesLoadError.value = true
                                repliesLoading.value = false
                            }
                        })
                )
            }
        }.addOnFailureListener {
            disposable.add(
                skipToItApi.getRepliesUnauthenticated(commentId, page)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<Comment>>() {
                        override fun onSuccess(replies: List<Comment>) {
                            commentReplies.value = replies
                            repliesLoadError.value = false
                            repliesLoading.value = false
                        }

                        override fun onError(e: Throwable) {
                            repliesLoadError.value = true
                            repliesLoading.value = false
                        }
                    })
            )
        }
    }

    override fun onCleared() {
        disposable.clear()
    }
}