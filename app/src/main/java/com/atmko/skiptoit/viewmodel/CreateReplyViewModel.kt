package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class CreateReplyViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient,
    private val skipToItDatabase: SkipToItDatabase
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val createdReply: MutableLiveData<Comment> = MutableLiveData()
    val createError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun createReply(parentId: String, comment: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItApi.createReply(parentId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Comment>() {
                            override fun onSuccess(reply: Comment) {

                                AppExecutors.getInstance().diskIO().execute {
                                    val lastPageTracker =
                                        getLastReplyPageTrackerForEpisode(parentId)
                                    if (lastPageTracker != null && lastPageTracker.nextPage == null) {
                                        deleteReplyPage(parentId, lastPageTracker.page)
                                    }

                                    createdReply.postValue(reply)
                                    createError.postValue(false)
                                    processing.postValue(false)
                                }
                            }

                            override fun onError(e: Throwable) {
                                createError.value = true
                                processing.value = false
                            }
                        })
                )
            }
        }
    }

    private fun getLastReplyPageTrackerForEpisode(parentId: String): CommentPageTracker? {
        val lastReplyToComment = skipToItDatabase.commentDao().getLastReply(parentId)
        lastReplyToComment?.let {
            return skipToItDatabase.commentPageTrackerDao()
                .getCommentPageTracker(lastReplyToComment.commentId)
        }
        return null
    }

    private fun deleteReplyPage(parentId: String, page: Int) {
        val pageComments = skipToItDatabase.commentPageTrackerDao()
            .getRepliesInPage(parentId, page)
        skipToItDatabase.commentDao().deleteComments(pageComments)
    }

    override fun onCleared() {
        disposable.clear()
    }
}