package com.atmko.skiptoit.viewmodel.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.util.AppExecutors
import com.atmko.skiptoit.viewmodel.paging.CommentBoundaryCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

open class CommentsViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient,
    private val commentDao: CommentDao,
    private val commentBoundaryCallback: CommentBoundaryCallback
) : ViewModel() {

    val TAG = this::class.simpleName

    companion object {
        const val pageSize = 20
        const val  enablePlaceholders = true
        const val  maxSize = 60
        const val  prefetchDistance = 5
        const val  initialLoadSize = 40
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    var retrievedComments: LiveData<PagedList<Comment>>? = null

    fun onUpVoteClick(comment: Comment) {
        when (comment.voteWeight) {
            VOTE_WEIGHT_UP_VOTE -> {
                comment.voteWeight = VOTE_WEIGHT_NONE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_DOWN_VOTE
                deleteCommentVote(comment)
            }
            VOTE_WEIGHT_NONE -> {
                comment.voteWeight = VOTE_WEIGHT_UP_VOTE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_UP_VOTE
                voteComment(comment, VOTE_WEIGHT_UP_VOTE)
            }
            else -> {
                comment.voteWeight = VOTE_WEIGHT_UP_VOTE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_DOWN_VOTE_INVERT
                voteComment(comment, VOTE_WEIGHT_UP_VOTE)
            }
        }
    }

    fun onDownVoteClick(comment: Comment) {
        when (comment.voteWeight) {
            VOTE_WEIGHT_DOWN_VOTE -> {
                comment.voteWeight = VOTE_WEIGHT_NONE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_UP_VOTE
                deleteCommentVote(comment)
            }
            VOTE_WEIGHT_NONE -> {
                comment.voteWeight = VOTE_WEIGHT_DOWN_VOTE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_DOWN_VOTE
                voteComment(comment, VOTE_WEIGHT_DOWN_VOTE)
            }
            else -> {
                comment.voteWeight = VOTE_WEIGHT_DOWN_VOTE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_UP_VOTE_INVERT
                voteComment(comment, VOTE_WEIGHT_DOWN_VOTE)
            }
        }
    }

    private fun voteComment(comment: Comment, voteWeight: Int) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.voteComment(comment.commentId, voteWeight.toString(), it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    AppExecutors.getInstance().diskIO().execute {
                                        commentDao.updateComment(comment)
                                        Log.d("VOTE SUCCESS: ", comment.voteWeight.toString())
                                    }
                                } else {
                                    Log.d(TAG, "Failure")
                                }
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        })
                )
            }
        }
    }

    private fun deleteCommentVote(comment: Comment) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.deleteCommentVote(comment.commentId, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    AppExecutors.getInstance().diskIO().execute {
                                        commentDao.updateComment(comment)
                                        Log.d("DELETE SUCCESS: ", comment.voteWeight.toString())
                                    }
                                } else {
                                    Log.d(TAG, "Failure")
                                }
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        })
                )
            }
        }
    }

    fun deleteComment(comment: Comment) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.deleteComment(comment.commentId, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    AppExecutors.getInstance().diskIO().execute {
                                        commentDao.deleteComments(listOf(comment))
                                        Log.d(TAG, "Success")
                                    }
                                } else {
                                    Log.d(TAG, "Failure")
                                }
                            }

                            override fun onError(e: Throwable) {

                            }
                        })
                )
            }
        }
    }

    fun getCommentLoading(): LiveData<Boolean> {
        return commentBoundaryCallback.loading
    }

    fun getCommentError(): LiveData<Boolean> {
        return commentBoundaryCallback.loadError
    }

    override fun onCleared() {
        disposable.clear()
    }
}