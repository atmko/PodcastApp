package com.atmko.skiptoit.createreply

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.database.CommentCache

class CreateReplyViewModel(
    private val createReplyEndpoint: CreateReplyEndpoint,
    private val commentCache: CommentCache
) : BaseViewModel<CreateReplyViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onLoadParentComment(fetchedComment: Comment)
        fun onLoadParentCommentFailed()
        fun onReplyCreated()
        fun onReplyCreateFailed()
        fun onPageTrackerFetchFailed()
        fun onReplyPageDeleteFailed()
        fun onUpdateReplyCountFailed()
    }

    var parentComment: Comment? = null

    fun getCachedParentCommentAndNotify(commentId: String) {
        if (parentComment != null) {
            notifyLoadParentCommentSuccess()
            return
        }

        notifyProcessing()

        commentCache.getCachedComment(commentId, object : CommentCache.CommentFetchListener {
            override fun onCommentFetchSuccess(fetchedComment: Comment?) {
                if (fetchedComment != null) {
                    parentComment = fetchedComment
                    notifyLoadParentCommentSuccess()
                } else {
                    notifyLoadParentCommentFailure()
                }
            }

            override fun onCommentFetchFailed() {
                notifyLoadParentCommentFailure()
            }
        })
    }

    fun createReplyAndNotify(parentId: String, replyBody: String) {
        notifyProcessing()

        createReplyEndpoint.createReply(parentId, replyBody, object : CreateReplyEndpoint.Listener {
            override fun onCreateSuccess(reply: Comment) {
                getLastReplyPageTrackerForEpisode(parentId)
            }

            override fun onCreateFailed() {
                notifyCreateReplyFailure()
            }
        })
    }

    private fun getLastReplyPageTrackerForEpisode(parentId: String) {
        commentCache.getLastReplyPageTracker(parentId, object : CommentCache.CommentPageTrackerListener {
            override fun onPageTrackerFetched(commentPageTracker: CommentPageTracker?) {
                if (commentPageTracker != null && commentPageTracker.nextPage == null) {
                    deleteReplyPage(parentId, commentPageTracker.page)
                } else {
                    updateParentCommentReplyCountAndNotify(parentId)
                }
            }

            override fun onPageTrackerFetchFailed() {
                notifyPageTrackerFetchFailure()
            }
        })
    }

    private fun deleteReplyPage(parentId: String, page: Int) {
        commentCache.deleteAllRepliesInPage(parentId, page, object : CommentCache.DeletePageListener {
            override fun onPageDeleted() {
                //todo: notify create reply success and update parent comment reply count and notify
                // might be able to run concurrently since create reply success call is not dependent on
                // parent comment reply count updating
                updateParentCommentReplyCountAndNotify(parentId)
            }

            override fun onPageDeleteFailed() {
                notifyReplyPageDeleteFailure()
            }
        })
    }

    fun updateParentCommentReplyCountAndNotify(commentId: String) {
        commentCache.increaseReplyCount(commentId, object : CommentCache.UpdateReplyCountListener {
            override fun onReplyCountUpdated() {
                notifyCreateReplySuccess()
            }

            override fun onReplyCountUpdateFailed() {
                notifyUpdateReplyCountFailure()
                notifyCreateReplySuccess()
            }
        })
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

    private fun notifyLoadParentCommentSuccess() {
        for (listener in listeners) {
            listener.onLoadParentComment(parentComment!!)
        }
    }

    private fun notifyLoadParentCommentFailure() {
        for (listener in listeners) {
            listener.onLoadParentCommentFailed()
        }
    }

    private fun notifyCreateReplySuccess() {
        for (listener in listeners) {
            listener.onReplyCreated()
        }
    }

    private fun notifyCreateReplyFailure() {
        for (listener in listeners) {
            listener.onReplyCreateFailed()
        }
    }

    private fun notifyPageTrackerFetchFailure() {
        for (listener in listeners) {
            listener.onPageTrackerFetchFailed()
        }
    }

    private fun notifyReplyPageDeleteFailure() {
        for (listener in listeners) {
            listener.onReplyPageDeleteFailed()
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