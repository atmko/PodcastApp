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
        fun onVoteUpdate()
        fun onVoteUpdateFailed()
        fun onDeleteComment()
        fun onWipeComment()
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
        commentBoundaryCallback.notifyPageLoading()
        commentEndpoint.voteComment(comment.commentId, voteWeight, object :
            CommentsEndpoint.VoteListener {
            override fun onVoteSuccess() {
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
        commentBoundaryCallback.notifyPageLoading()
        commentEndpoint.deleteCommentVote(comment.commentId, object :
            CommentsEndpoint.VoteListener {
            override fun onVoteSuccess() {
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
        commentBoundaryCallback.notifyPageLoading()
        commentEndpoint.deleteComment(comment.commentId, object : CommentsEndpoint.DeleteListener {
            override fun onDeleteSuccess() {
                if (comment.parentId == null || comment.replies == 0) {
                    deleteLocalComment(comment)
                } else {
                    wipeLocalComment(comment)
                }
            }

            override fun onDeleteFailed() {
                notifyDeleteFailure()
            }
        })
    }

    private fun deleteLocalComment(comment: Comment) {
        commentCache.deleteComments(listOf(comment), object : CommentCache.CacheUpdateListener {
            override fun onLocalCacheUpdateSuccess() {
                //todo: notify delete success and update parent comment reply count and notify
                // might be able to run concurrently since delete success call is not dependent on
                // parent comment reply count updating
                if (comment.parentId != null) {
                    updateParentCommentReplyCountAndNotify(comment.parentId, comment.commentId)
                } else {
                    notifyDeleteSuccess(comment.commentId)
                }
            }
        })
    }

    private fun wipeLocalComment(comment: Comment) {
        commentCache.wipeComment(comment.commentId, object : CommentCache.CacheUpdateListener {
            override fun onLocalCacheUpdateSuccess() {
                //todo: notify delete success and update parent comment reply count and notify
                // might be able to run concurrently since delete success call is not dependent on
                // parent comment reply count updating
                notifyWipeSuccess(comment.commentId)
            }
        })
    }

    private fun updateParentCommentReplyCountAndNotify(parentCommentId: String, commentId: String) {
        commentCache.decreaseReplyCount(parentCommentId, object : CommentCache.UpdateReplyCountListener {
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

    protected open fun notifyWipeSuccess(commentId: String) {
        for (listener in listeners) {
            listener.onWipeComment()
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