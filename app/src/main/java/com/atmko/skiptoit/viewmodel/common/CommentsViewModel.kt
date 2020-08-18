package com.atmko.skiptoit.viewmodel.common

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

open class CommentsViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    val TAG = this::class.simpleName

    private val disposable: CompositeDisposable = CompositeDisposable()

    val retrievedComments: MutableLiveData<ArrayList<Comment>> = MutableLiveData()

    //updates to this reflect local comment changes and not server
    val localCommentVoteUpdate: MutableLiveData<CommentUpdate> = MutableLiveData()

    fun onUpVoteClick(comment: Comment, position: Int) {
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

        localCommentVoteUpdate.value = CommentUpdate(comment, position)
        localCommentVoteUpdate.value = null
    }

    fun onDownVoteClick(comment: Comment, position: Int) {
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

        localCommentVoteUpdate.value = CommentUpdate(comment, position)
        localCommentVoteUpdate.value = null
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
                                Log.d(TAG, "Success")
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
                                Log.d(TAG, "Success")
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        })
                )
            }
        }
    }

    //updates to this reflect local comment changes and not server
    val deleteCommentUpdate: MutableLiveData<CommentUpdate> = MutableLiveData()

    fun deleteComment(comment: Comment, position: Int) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.deleteComment(comment.commentId, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    deleteCommentUpdate.value = CommentUpdate(comment, position)
                                    deleteCommentUpdate.value = null
                                }
                            }

                            override fun onError(e: Throwable) {

                            }
                        })
                )
            }
        }
    }

    fun addComment(comment: Comment) {
        retrievedComments.value?.add(comment)
    }

    fun removeComment(position: Int) {
        retrievedComments.value?.removeAt(position)
    }

    override fun onCleared() {
        disposable.clear()
    }
}