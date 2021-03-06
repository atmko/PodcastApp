package com.atmko.skiptoit.updatecomment

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache

open class UpdateCommentViewModel(
    private val updateCommentEndpoint: UpdateCommentEndpoint,
    private val commentCache: CommentCache
) : BaseViewModel<UpdateCommentViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onLoadComment(fetchedComment: Comment)
        fun onLoadCommentFailed()
        fun onCommentUpdated()
        fun onCommentUpdateFailed()
    }

    var comment: Comment? = null

    fun getCachedCommentAndNotify(commentId: String) {
        if (comment != null) {
            notifyLoadCommentSuccess()
            return
        }

        notifyProcessing()

        commentCache.getCachedComment(commentId, object : CommentCache.CommentFetchListener {
            override fun onCommentFetchSuccess(fetchedComment: Comment?) {
                if (fetchedComment != null) {
                    comment = fetchedComment
                    notifyLoadCommentSuccess()
                } else {
                    notifyLoadCommentFailure()
                }
            }

            override fun onCommentFetchFailed() {
                notifyLoadCommentFailure()
            }
        })
    }

    fun updateCommentBodyAndNotify(bodyUpdate : String) {
        notifyProcessing()

        updateCommentEndpoint.updateComment(comment!!, bodyUpdate, object : UpdateCommentEndpoint.Listener {
            override fun onUpdateSuccess(bodyUpdate: String) {
                comment!!.body = bodyUpdate
                commentCache.updateLocalCache(comment!!, object : CommentCache.CacheUpdateListener {
                    override fun onLocalCacheUpdateSuccess() {
                        notifyUpdateSuccess()
                    }
                })
            }

            override fun onUpdateFailed() {
                notifyUpdateFailure()
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

    private fun notifyLoadCommentSuccess() {
        for (listener in listeners) {
            listener.onLoadComment(comment!!)
        }
    }

    private fun notifyLoadCommentFailure() {
        for (listener in listeners) {
            listener.onLoadCommentFailed()
        }
    }

    private fun notifyUpdateSuccess() {
        for (listener in listeners) {
            listener.onCommentUpdated()
        }
    }

    private fun notifyUpdateFailure() {
        for (listener in listeners) {
            listener.onCommentUpdateFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}