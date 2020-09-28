package com.atmko.skiptoit.createreply

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.model.database.CommentPageTrackerDao
import com.atmko.skiptoit.util.AppExecutors

open class ReplyPageTrackerHelper(
    private val commentDao: CommentDao?,
    private val commentPageTrackerDao: CommentPageTrackerDao?
) {

    interface Listener {
        fun onPagingDataUpdated(reply: Comment)
    }

    open fun updatePagingTracker(reply: Comment, listener: Listener) {
        AppExecutors.getInstance().diskIO().execute {
            val lastPageTracker =
                getLastReplyPageTrackerForEpisode(reply.parentId!!)
            if (lastPageTracker != null && lastPageTracker.nextPage == null) {
                deleteReplyPage(reply.parentId, lastPageTracker.page)
            }

            AppExecutors.getInstance().mainThread().execute {
                listener.onPagingDataUpdated(reply)
            }
        }
    }

    private fun getLastReplyPageTrackerForEpisode(parentId: String): CommentPageTracker? {
        val lastReplyToComment = commentDao!!.getLastReply(parentId)
        lastReplyToComment?.let {
            return commentPageTrackerDao!!.getCommentPageTracker(lastReplyToComment.commentId)
        }
        return null
    }

    private fun deleteReplyPage(parentId: String, page: Int) {
        val pageComments = commentPageTrackerDao!!.getRepliesInPage(parentId, page)
        commentDao!!.deleteComments(pageComments)
    }
}