package com.atmko.skiptoit.createcomment

import android.util.Log
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.model.database.CommentCache.UpdatePagingDataListener
import com.atmko.skiptoit.common.BaseViewModel

class CreateCommentViewModel(
    private val createCommentEndpoint: CreateCommentEndpoint,
    private val commentCache: CommentCache
) : BaseViewModel<CreateCommentViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onCommentCreated(comment: Comment?)
        fun onCommentCreateFailed()
    }

    fun createCommentAndNotify(podcastId: String, episodeId: String, commentBody: String) {
        notifyProcessing()

        createCommentEndpoint.createComment(podcastId, episodeId, commentBody,  object : CreateCommentEndpoint.Listener {
            override fun onCreateSuccess(comment: Comment) {
                commentCache.updateCommentPagingTracker(comment, object : UpdatePagingDataListener {
                    override fun onPagingDataUpdated(comment: Comment) {
                        notifyCreateCommentSuccess(comment)
                    }
                })
            }

            override fun onCreateFailed() {
                notifyCreateCommentFailure()
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

    private fun notifyCreateCommentSuccess(comment: Comment) {
        for (listener in listeners) {
            listener.onCommentCreated(comment)
        }
    }

    private fun notifyCreateCommentFailure() {
        for (listener in listeners) {
            listener.onCommentCreateFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}