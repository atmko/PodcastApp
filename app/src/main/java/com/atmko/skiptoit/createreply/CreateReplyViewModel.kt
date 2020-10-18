package com.atmko.skiptoit.createreply

import android.util.Log
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.model.database.CommentCache.UpdatePagingDataListener
import com.atmko.skiptoit.common.BaseViewModel

class CreateReplyViewModel(
    private val createReplyEndpoint: CreateReplyEndpoint,
    private val commentCache: CommentCache
) : BaseViewModel<CreateReplyViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onReplyCreated(comment: Comment?)
        fun onReplyCreateFailed()
    }

    fun createReplyAndNotify(parentId: String, replyBody: String) {
        notifyProcessing()

        createReplyEndpoint.createReply(parentId, replyBody, object : CreateReplyEndpoint.Listener {
            override fun onCreateSuccess(reply: Comment) {
                commentCache.updateReplyPagingTracker(parentId, object : UpdatePagingDataListener {
                    override fun onPagingDataUpdated() {
                        notifyCreateReplySuccess(reply)
                    }
                })
            }

            override fun onCreateFailed() {
                notifyCreateReplyFailure()
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

    private fun notifyCreateReplySuccess(comment: Comment) {
        for (listener in listeners) {
            listener.onReplyCreated(comment)
        }
    }

    private fun notifyCreateReplyFailure() {
        for (listener in listeners) {
            listener.onReplyCreateFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}