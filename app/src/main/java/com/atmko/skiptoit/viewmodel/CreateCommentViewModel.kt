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

class CreateCommentViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient,
    private val skipToItDatabase: SkipToItDatabase
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val createdComment: MutableLiveData<Comment> = MutableLiveData()
    val createError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun createComment(podcastId: String, episodeId: String, comment: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItApi.createComment(podcastId, episodeId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Comment>() {
                            override fun onSuccess(comment: Comment) {

                                AppExecutors.getInstance().diskIO().execute {
                                    val lastPageTracker =
                                        getLastCommentPageTrackerForEpisode(episodeId)
                                    if (lastPageTracker != null && lastPageTracker.nextPage == null) {
                                        deleteCommentPage(episodeId, lastPageTracker.page)
                                    }

                                    createdComment.postValue(comment)
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

    private fun getLastCommentPageTrackerForEpisode(episodeId: String): CommentPageTracker? {
        val lastCommentInEpisode = skipToItDatabase.commentDao().getLastComment(episodeId)
        lastCommentInEpisode?.let {
            return skipToItDatabase.commentPageTrackerDao()
                .getCommentPageTracker(lastCommentInEpisode.commentId)
        }
        return null
    }

    private fun deleteCommentPage(episodeId: String, page: Int) {
        val pageComments = skipToItDatabase.commentPageTrackerDao()
            .getCommentsInPage(episodeId, page)
        skipToItDatabase.commentDao().deleteComments(pageComments)
    }

    override fun onCleared() {
        disposable.clear()
    }
}