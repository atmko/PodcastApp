package com.atmko.skiptoit.createcomment

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.model.database.CommentPageTrackerDao
import com.atmko.skiptoit.util.AppExecutors

open class CommentPageTrackerHelper(
    private val commentDao: CommentDao?,
    private val commentPageTrackerDao: CommentPageTrackerDao?
) {

    interface Listener {
        fun onPagingDataUpdated(comment: Comment)
    }

    open fun updatePagingTracker(comment: Comment, listener: Listener) {
        AppExecutors.getInstance().diskIO().execute {
            val lastPageTracker =
                getLastCommentPageTrackerForEpisode(comment.episodeId)
            if (lastPageTracker != null && lastPageTracker.nextPage == null) {
                deleteCommentPage(comment.episodeId, lastPageTracker.page)
            }

            AppExecutors.getInstance().mainThread().execute {
                listener.onPagingDataUpdated(comment)
            }
        }
    }

    private fun getLastCommentPageTrackerForEpisode(episodeId: String): CommentPageTracker? {
        val lastCommentInEpisode = commentDao!!.getLastComment(episodeId)
        lastCommentInEpisode?.let {
            return commentPageTrackerDao!!.getCommentPageTracker(lastCommentInEpisode.commentId)
        }
        return null
    }

    private fun deleteCommentPage(episodeId: String, page: Int) {
        val pageComments = commentPageTrackerDao!!.getCommentsInPage(episodeId, page)
        commentDao!!.deleteComments(pageComments)
    }
}