package com.atmko.skiptoit.episode.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.CommentCache

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
        fun onUpdateReplyCountFailed()
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
                        notifyVoteSuccess(comment.commentId)
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
                        notifyVoteSuccess(comment.commentId)
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

        commentEndpoint.deleteComment(comment, object : CommentsEndpoint.DeleteListener {
            override fun onDeleteSuccess() {
                deleteLocalComment(comment)
            }

            override fun onDeleteFailed() {
                notifyDeleteFailure()
            }
        })
    }

    private fun deleteLocalComment(comment: Comment) {
        commentCache.deleteComments(listOf(comment), object : CommentCache.CacheUpdateListener {
            override fun onLocalCacheUpdateSuccess() {
                if (comment.parentId != null) {
                    updateParentCommentReplyCountAndNotify(comment.parentId)
                } else {
                    notifyDeleteSuccess(comment.commentId)
                }
            }
        })
    }

    fun updateParentCommentReplyCountAndNotify(commentId: String) {
        commentCache.decreaseReplyCount(commentId, object : CommentCache.UpdateReplyCountListener {
            override fun onReplyCountUpdated() {
                notifyDeleteSuccess(commentId)
            }

            override fun onReplyCountUpdateFailed() {
                notifyUpdateReplyCountFailure()
                notifyDeleteSuccess(commentId)
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

    protected open fun notifyVoteSuccess(commentId: String) {
        for (listener in listeners) {
            listener.onVoteUpdate()
        }
    }

    private fun notifyVoteFailure() {
        for (listener in listeners) {
            listener.onVoteUpdateFailed()
        }
    }

    protected open fun notifyDeleteSuccess(commentId: String) {
        for (listener in listeners) {
            listener.onDeleteComment()
        }
    }

    private fun notifyDeleteFailure() {
        for (listener in listeners) {
            listener.onDeleteCommentFailed()
        }
    }

    private fun notifyUpdateReplyCountFailure() {
        for (listener in listeners) {
            listener.onUpdateReplyCountFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }

}