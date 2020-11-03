package com.atmko.skiptoit.createcomment

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.database.CommentCache

class CreateCommentViewModel(
    private val createCommentEndpoint: CreateCommentEndpoint,
    private val commentCache: CommentCache
) : BaseViewModel<CreateCommentViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onCommentCreated()
        fun onCommentCreateFailed()
        fun onPageTrackerFetchFailed()
        fun onCommentPageDeleteFailed()
    }

    fun createCommentAndNotify(podcastId: String, episodeId: String, commentBody: String) {
        notifyProcessing()

        createCommentEndpoint.createComment(podcastId, episodeId, commentBody,  object : CreateCommentEndpoint.Listener {
            override fun onCreateSuccess(comment: Comment) {
                getLastCommentPageTracker(episodeId)
            }

            override fun onCreateFailed() {
                notifyCreateCommentFailure()
            }
        })
    }

    private fun getLastCommentPageTracker(episodeId: String) {
        commentCache.getLastCommentPageTracker(episodeId, object : CommentCache.CommentPageTrackerListener {
            override fun onPageTrackerFetched(commentPageTracker: CommentPageTracker?) {
                if (commentPageTracker != null && commentPageTracker.nextPage == null) {
                    deleteCommentPage(episodeId, commentPageTracker.page)
                } else {
                    notifyCreateCommentSuccess()
                }
            }

            override fun onPageTrackerFetchFailed() {
                notifyPageTrackerFetchFailure()
                notifyCreateCommentSuccess()
            }
        })
    }

    private fun deleteCommentPage(episodeId: String, page: Int) {
        commentCache.deleteAllCommentsInPage(episodeId, page, object : CommentCache.DeletePageListener {
            override fun onPageDeleted() {
                notifyCreateCommentSuccess()
            }

            override fun onPageDeleteFailed() {
                notifyCommentPageDeleteFailure()
                notifyCreateCommentSuccess()
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

    private fun notifyCreateCommentSuccess() {
        for (listener in listeners) {
            listener.onCommentCreated()
        }
    }

    private fun notifyCreateCommentFailure() {
        for (listener in listeners) {
            listener.onCommentCreateFailed()
        }
    }

    private fun notifyPageTrackerFetchFailure() {
        for (listener in listeners) {
            listener.onPageTrackerFetchFailed()
        }
    }

    private fun notifyCommentPageDeleteFailure() {
        for (listener in listeners) {
            listener.onCommentPageDeleteFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}