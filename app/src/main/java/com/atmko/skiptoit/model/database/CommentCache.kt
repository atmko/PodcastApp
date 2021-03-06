package com.atmko.skiptoit.model.database

import com.atmko.skiptoit.common.BaseBoundaryCallback.Companion.loadTypeRefresh
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.utils.AppExecutors

open class CommentCache(
    private val skipToItDatabase: SkipToItDatabase?,
    private val appExecutors: AppExecutors?
) {

    interface CommentFetchListener {
        fun onCommentFetchSuccess(fetchedComment: Comment?)
        fun onCommentFetchFailed()
    }

    interface CacheUpdateListener {
        fun onLocalCacheUpdateSuccess()
    }

    interface UpdatePagingDataListener {
        fun onPagingDataUpdated()
    }

    interface PageFetchListener {
        fun onPageFetchSuccess()
        fun onPageFetchFailed()
    }

    interface CommentPageTrackerListener {
        fun onPageTrackerFetched(commentPageTracker: CommentPageTracker?)
        fun onPageTrackerFetchFailed()
    }

    interface DeletePageListener {
        fun onPageDeleted()
        fun onPageDeleteFailed()
    }

    interface UpdateReplyCountListener {
        fun onReplyCountUpdated()
        fun onReplyCountUpdateFailed()
    }

    open fun getCachedComment(commentId: String, listener: CommentFetchListener) {
        appExecutors!!.diskIO.execute {
            val fetchedComment = skipToItDatabase!!.commentDao().getComment(commentId)

            appExecutors.mainThread.execute {
                listener.onCommentFetchSuccess(fetchedComment)
            }
        }
    }

    //--------------
    open fun updateLocalCache(updatedComment: Comment, listener: CacheUpdateListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().updateComment(updatedComment)

            appExecutors.mainThread.execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }

    open fun deleteComments(comments: List<Comment>, listener: CacheUpdateListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().deleteComments(comments)

            appExecutors.mainThread.execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }

    open fun wipeComment(commentId: String, listener: CacheUpdateListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().wipeComment(commentId)

            appExecutors.mainThread.execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }

    //--------------

    open fun getLastCommentPageTracker(episodeId: String, listener: CommentPageTrackerListener) {
        appExecutors!!.diskIO.execute {
            val commentPageTracker = skipToItDatabase!!.commentPageTrackerDao().getLastCommentPageTracker(episodeId)

            appExecutors.mainThread.execute {
                listener.onPageTrackerFetched(commentPageTracker)
            }
        }
    }

    open fun deleteAllCommentsInPage(episodeId: String, page: Int, listener: DeletePageListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().deleteAllCommentsInPage(episodeId, page)

            appExecutors.mainThread.execute {
                listener.onPageDeleted()
            }
        }
    }

    open fun getLastReplyPageTracker(parentId: String, listener: CommentPageTrackerListener) {
        appExecutors!!.diskIO.execute {
            val commentPageTracker = skipToItDatabase!!.commentPageTrackerDao().getLastReplyPageTracker(parentId)

            appExecutors.mainThread.execute {
                listener.onPageTrackerFetched(commentPageTracker)
            }
        }
    }

    open fun deleteAllRepliesInPage(parentId: String, page: Int, listener: DeletePageListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().deleteAllRepliesInPage(parentId, page)

            appExecutors.mainThread.execute {
                listener.onPageDeleted()
            }
        }
    }

    open fun increaseReplyCount(commentId: String, listener: UpdateReplyCountListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().increaseReplyCount(commentId)

            appExecutors.mainThread.execute {
                listener.onReplyCountUpdated()
            }
        }
    }

    open fun decreaseReplyCount(commentId: String, listener: UpdateReplyCountListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.commentDao().decreaseReplyCount(commentId)

            appExecutors.mainThread.execute {
                listener.onReplyCountUpdated()
            }
        }
    }

    //--------------
    open fun getPageTracker(commentId: String, listener: CommentPageTrackerListener) {
        appExecutors!!.diskIO.execute {
            val commentPageTracker =
                skipToItDatabase!!.commentPageTrackerDao().getCommentPageTracker(commentId)

            appExecutors.mainThread.execute {
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
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.beginTransaction()
            try {
                if (loadType == loadTypeRefresh) {
                    skipToItDatabase.commentDao().deleteAllReplies(param)
                }

                val pageTrackers = extractPageTrackers(commentResults)

                saveCommentsAndPageTrackers(commentResults, pageTrackers)
                skipToItDatabase.setTransactionSuccessful()
            } finally {
                skipToItDatabase.endTransaction()
            }

            appExecutors.mainThread.execute {
                listener.onPageFetchSuccess()
            }
        }
    }

    open fun insertCommentsAndTrackers(
        commentResults: CommentResults,
        loadType: Int,
        listener: PageFetchListener
    ) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.beginTransaction()
            try {
                if (loadType == loadTypeRefresh) {
                    skipToItDatabase.commentDao().deleteAllComments()
                }

                val pageTrackers = extractPageTrackers(commentResults)

                saveCommentsAndPageTrackers(commentResults, pageTrackers)
                skipToItDatabase.setTransactionSuccessful()
            } finally {
                skipToItDatabase.endTransaction()
            }

            appExecutors.mainThread.execute {
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