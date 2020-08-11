package com.atmko.skiptoit.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.view.adapters.CommentsAdapter
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

val TAG = CommentsViewModel::class.simpleName
class CommentsViewModel(private val skipToItApi: SkipToItApi,
                        private val googleSignInClient: GoogleSignInClient,
                        private val application: SkipToItApplication) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val isCreated: MutableLiveData<Boolean> = MutableLiveData()
    val createError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun createComment(podcastId: String, episodeId: String, comment: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                isCreated.value = false
                processing.value = true
                disposable.add(
                    skipToItApi.createComment(podcastId, episodeId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                isCreated.value = response.isSuccessful
                                createError.value = false
                                processing.value = false
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

    fun createReply(parentId: String, comment: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                isCreated.value = false
                processing.value = true
                disposable.add(
                    skipToItApi.createReply(parentId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                isCreated.value = response.isSuccessful
                                createError.value = false
                                processing.value = false
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

    val episodeComments:MutableLiveData<List<Comment>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getComments(episodeId: String, page: Int) {
        loading.value = true
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.getCommentsAuthenticated(episodeId, page, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<Comment>>() {
                            override fun onSuccess(comments: List<Comment>) {
                                episodeComments.value = comments
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
                    .subscribeWith(object : DisposableSingleObserver<List<Comment>>() {
                        override fun onSuccess(comments: List<Comment>) {
                            episodeComments.value = comments
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

    //live data value holds map of type (String : MutableList<Any>)
    //MutableList<Any> holds two items (Comment i.e parentComment and List<Comments> i.e replies)
    val repliesMap: MutableLiveData<HashMap<String, MutableList<Any>>> = MutableLiveData()
    val repliesLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val repliesLoading: MutableLiveData<Boolean> = MutableLiveData()

    //helper method to save parent comment
    fun saveParentComment(comment: Comment) {
        if (repliesMap.value != null) {
            repliesMap.value!![comment.commentId] = mutableListOf(comment, listOf<Comment>())
        } else {
            val map = HashMap<String, MutableList<Any>>()
            map[comment.commentId] = mutableListOf(comment, listOf<Comment>())
            repliesMap.value = map
        }
    }

    //helper method to delete parent comment
    fun removeParentComment(commentId: String) {
        repliesMap.value?.remove(commentId)
    }

    //helper method to save replies in repliesMap
    private fun insertReplies(parentId: String, replies: List<Comment>) {
        val currentValue = repliesMap.value
        currentValue!![parentId]!![1] = replies

        repliesMap.value = currentValue
    }

    //helper method to access already saved replies
    @Suppress("UNCHECKED_CAST")
    fun retrieveReplies(commentId: String): List<Comment> {
        return repliesMap.value!![commentId]!![1] as List<Comment>
    }

    //helper method to access already saved parent comment
    @Suppress("UNCHECKED_CAST")
    fun retrieveParentComment(commentId: String): Comment {
        return repliesMap.value!![commentId]!![0] as Comment
    }

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
                                insertReplies(commentId, replies)
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
                            insertReplies(commentId, replies)
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

    fun onUpVoteClick(commentsAdapter: CommentsAdapter, comment: Comment, position: Int) {
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

        commentsAdapter.updateCommentVote(comment, position)
    }

    fun onDownVoteClick(commentsAdapter: CommentsAdapter, comment: Comment, position: Int) {
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

        commentsAdapter.updateCommentVote(comment, position)
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

    override fun onCleared() {
        disposable.clear()
    }
}