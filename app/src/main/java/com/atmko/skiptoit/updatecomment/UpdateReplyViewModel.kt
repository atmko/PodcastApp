package com.atmko.skiptoit.updatecomment

import android.util.Log
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache

class UpdateReplyViewModel(
    updateCommentEndpoint: UpdateCommentEndpoint,
    private val commentCache: CommentCache
) : UpdateCommentViewModel(updateCommentEndpoint, commentCache) {

    interface Listener {
        fun onLoadParentComment(fetchedComment: Comment)
        fun onLoadParentCommentFailed()
    }

    private val replyViewModelListeners = mutableListOf<Listener>()

    fun registerReplyViewModelListener(listener: Listener) {
        replyViewModelListeners.add(listener)
    }

    fun unregisterReplyViewModelListener(listener: Listener) {
        replyViewModelListeners.remove(listener)
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
        for (listener in replyViewModelListeners) {
            listener.onLoadParentComment(parentComment!!)
        }
    }

    private fun notifyLoadParentCommentFailure() {
        for (listener in replyViewModelListeners) {
            listener.onLoadParentCommentFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}