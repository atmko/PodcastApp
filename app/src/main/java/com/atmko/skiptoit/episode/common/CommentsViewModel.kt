package com.atmko.skiptoit.episode.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.common.BaseViewModel

open class CommentsViewModel(
    private val commentEndpoint: CommentsEndpoint,
    private val commentCache: CommentCache,
    private val commentBoundaryCallback: CommentBoundaryCallback
) : BaseViewModel<CommentsViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onVoteUpdate()
        fun onVoteUpdateFailed()
        fun onDeleteComment()
        fun onDeleteCommentFailed()
    }

    companion object {
        const val pageSize = 20
        const val enablePlaceholders = true
        const val maxSize = 60
        const val prefetchDistance = 5
        const val initialLoadSize = 40
    }

    var retrievedComments: LiveData<PagedList<Comment>>? = null

    fun upVoteAndNotify(comment: Comment) {
        when (comment.voteWeight) {
            VOTE_WEIGHT_UP_VOTE -> {
                comment.voteWeight = VOTE_WEIGHT_NONE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_NEUTRALIZE_UP_VOTE
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

    fun downVoteAndNotify(comment: Comment) {
        when (comment.voteWeight) {
            VOTE_WEIGHT_DOWN_VOTE -> {
                comment.voteWeight = VOTE_WEIGHT_NONE
                comment.voteTally = comment.voteTally + VOTE_WEIGHT_NEUTRALIZE_DOWN_VOTE
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
        notifyProcessing()

        commentEndpoint.voteComment(comment, voteWeight, object :
            CommentsEndpoint.VoteListener {
            override fun onVoteSuccess(comment: Comment) {
                commentCache.updateLocalCache(comment, object : CommentCache.CacheUpdateListener {
                    override fun onLocalCacheUpdateSuccess() {
                        notifyVoteSuccess()
                    }
                })
            }

            override fun onVoteFailed() {
                notifyVoteFailure()
            }
        })
    }

    private fun deleteCommentVote(comment: Comment) {
        notifyProcessing()

        commentEndpoint.deleteCommentVote(comment, object :
            CommentsEndpoint.VoteListener {
            override fun onVoteSuccess(comment: Comment) {
                commentCache.updateLocalCache(comment, object : CommentCache.CacheUpdateListener {
                    override fun onLocalCacheUpdateSuccess() {
                        notifyVoteSuccess()
                    }
                })
            }

            override fun onVoteFailed() {
                notifyVoteFailure()
            }
        })
    }

    fun deleteCommentAndNotify(comment: Comment) {
        notifyProcessing()

        commentEndpoint.deleteComment(comment, object :
            CommentsEndpoint.DeleteListener {
            override fun onDeleteSuccess() {
                commentCache.deleteComments(listOf(comment), object : CommentCache.CacheUpdateListener {
                        override fun onLocalCacheUpdateSuccess() {
                            notifyDeleteSuccess()
                        }
                    })
            }

            override fun onDeleteFailed() {
                notifyDeleteFailure()
            }
        })
    }

    fun registerBoundaryCallbackListener(listener: BaseBoundaryCallback.Listener) {
        commentBoundaryCallback.registerListener(listener)
    }

    fun unregisterBoundaryCallbackListener(listener: BaseBoundaryCallback.Listener) {
        commentBoundaryCallback.unregisterListener(listener)
    }

    private fun unregisterListeners() {
        for (listener in listeners) {
            unregisterListener(listener)
        }
    }

    private fun notifyProcessing() {
        for (listener in listeners) {
            listener.notifyProcessing()
        }
    }

    private fun notifyVoteSuccess() {
        for (listener in listeners) {
            listener.onVoteUpdate()
        }
    }

    private fun notifyVoteFailure() {
        for (listener in listeners) {
            listener.onVoteUpdateFailed()
        }
    }

    private fun notifyDeleteSuccess() {
        for (listener in listeners) {
            listener.onDeleteComment()
        }
    }

    private fun notifyDeleteFailure() {
        for (listener in listeners) {
            listener.onDeleteCommentFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }

}