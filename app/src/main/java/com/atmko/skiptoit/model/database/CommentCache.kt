package com.atmko.skiptoit.model.database

import com.atmko.skiptoit.episode.common.CommentBoundaryCallback
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.util.AppExecutors

open class CommentCache(
    private val skipToItDatabase: SkipToItDatabase?
) {

    interface CommentFetchListener {
        fun onCommentFetchSuccess(fetchedComment: Comment)
        fun onCommentFetchFailed()
    }

    interface CacheUpdateListener {
        fun onLocalCacheUpdateSuccess()
    }

    interface UpdatePagingDataListener {
        fun onPagingDataUpdated(comment: Comment)
    }

    interface PageFetchListener {
        fun onPageFetchSuccess()
        fun onPageFetchFailed()
    }

    interface CommentPageTrackerListener {
        fun onPageTrackerFetched(commentPageTracker: CommentPageTracker)
        fun onPageTrackerFetchFailed()
    }

    open fun getCachedComment(commentId: String, listener: CommentFetchListener) {
        AppExecutors.getInstance().diskIO().execute {
            val fetchedComment = skipToItDatabase!!.commentDao().getComment(commentId)

            AppExecutors.getInstance().mainThread().execute {
                listener.onCommentFetchSuccess(fetchedComment)
            }
        }
    }

    //--------------
    open fun updateLocalCache(updatedComment: Comment, listener: CacheUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            skipToItDatabase!!.commentDao().updateComment(updatedComment)

            AppExecutors.getInstance().mainThread().execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }

    open fun deleteComments(comments: List<Comment>, listener: CacheUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            skipToItDatabase!!.commentDao().deleteComments(comments)

            AppExecutors.getInstance().mainThread().execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }

    //--------------
    open fun updateCommentPagingTracker(comment: Comment, listener: UpdatePagingDataListener) {
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
        val lastCommentInEpisode = skipToItDatabase!!.commentDao().getLastComment(episodeId)
        lastCommentInEpisode?.let {
            return skipToItDatabase.commentPageTrackerDao().getCommentPageTracker(lastCommentInEpisode.commentId)
        }
        return null
    }

    private fun deleteCommentPage(episodeId: String, page: Int) {
        val pageComments = skipToItDatabase!!.commentPageTrackerDao().getCommentsInPage(episodeId, page)
        skipToItDatabase.commentDao().deleteComments(pageComments)
    }

    open fun updateReplyPagingTracker(reply: Comment, listener: UpdatePagingDataListener) {
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
        val lastReplyToComment = skipToItDatabase!!.commentDao().getLastReply(parentId)
        lastReplyToComment?.let {
            return skipToItDatabase.commentPageTrackerDao().getCommentPageTracker(lastReplyToComment.commentId)
        }
        return null
    }

    private fun deleteReplyPage(parentId: String, page: Int) {
        val pageComments = skipToItDatabase!!.commentPageTrackerDao().getRepliesInPage(parentId, page)
        skipToItDatabase.commentDao().deleteComments(pageComments)
    }

    //--------------
    open fun getPageTrackers(commentId: String, listener: CommentPageTrackerListener) {
        AppExecutors.getInstance().diskIO().execute {
            val commentPageTracker =
                skipToItDatabase!!.commentPageTrackerDao().getCommentPageTracker(commentId)

            AppExecutors.getInstance().mainThread().execute {
                listener.onPageTrackerFetched(commentPageTracker)
            }
        }
    }

    open fun insertRepliesAndTrackers(
        commentResults: CommentResults,
        loadType: Int,
        param: String,
        listener: PageFetchListener
    ) {
        AppExecutors.getInstance().diskIO().execute {
            skipToItDatabase!!.beginTransaction()
            try {
                if (loadType == CommentBoundaryCallback.loadTypeRefresh) {
                    skipToItDatabase.commentDao().deleteAllReplies(param)
                }

                val pageTrackers = extractPageTrackers(commentResults)

                saveCommentsAndPageTrackers(commentResults, pageTrackers)
                skipToItDatabase.setTransactionSuccessful()
            } finally {
                skipToItDatabase.endTransaction()
            }

            AppExecutors.getInstance().mainThread().execute {
                listener.onPageFetchSuccess()
            }
        }
    }

    open fun insertCommentsAndTrackers(
        commentResults: CommentResults,
        loadType: Int,
        listener: PageFetchListener
    ) {
        AppExecutors.getInstance().diskIO().execute {
            skipToItDatabase!!.beginTransaction()
            try {
                if (loadType == CommentBoundaryCallback.loadTypeRefresh) {
                    skipToItDatabase.commentDao().deleteAllComments()
                }

                val pageTrackers = extractPageTrackers(commentResults)

                saveCommentsAndPageTrackers(commentResults, pageTrackers)
                skipToItDatabase.setTransactionSuccessful()
            } finally {
                skipToItDatabase.endTransaction()
            }

            AppExecutors.getInstance().mainThread().execute {
                listener.onPageFetchSuccess()
            }
        }
    }

    private fun saveCommentsAndPageTrackers(
        commentResults: CommentResults,
        pageTrackers: MutableList<CommentPageTracker>
    ) {
        skipToItDatabase!!.commentDao().insertComments(commentResults.comments)
        skipToItDatabase.commentPageTrackerDao().insert(pageTrackers)
    }

    private fun extractPageTrackers(commentResults: CommentResults): MutableList<CommentPageTracker> {
        val comments = commentResults.comments
        val prevPage = if (!commentResults.hasPrev) null else commentResults.page - 1
        val nextPage = if (!commentResults.hasNext) null else commentResults.page + 1
        val pageTrackers = mutableListOf<CommentPageTracker>()
        for (i in comments) {
            pageTrackers.add(
                CommentPageTracker(
                    i.commentId,
                    commentResults.page,
                    nextPage,
                    prevPage
                )
            )
        }
        return pageTrackers
    }
}