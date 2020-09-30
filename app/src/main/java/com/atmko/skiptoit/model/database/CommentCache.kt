package com.atmko.skiptoit.model.database

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.util.AppExecutors

open class CommentCache(val commentDao: CommentDao?) {

    interface CommentFetchListener {
        fun onCommentFetchSuccess(fetchedComment: Comment)
        fun onCommentFetchFailed()
    }

    interface CacheUpdateListener {
        fun onLocalCacheUpdateSuccess()
    }

    open fun getCachedComment(commentId: String, listener: CommentFetchListener) {
        AppExecutors.getInstance().diskIO().execute {
            val fetchedComment = commentDao!!.getComment(commentId)

            AppExecutors.getInstance().mainThread().execute {
                listener.onCommentFetchSuccess(fetchedComment)
            }
        }
    }

    open fun updateLocalCache(updatedComment: Comment, listener: CacheUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            commentDao!!.updateComment(updatedComment)

            AppExecutors.getInstance().mainThread().execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }

    open fun deleteComments(comments: List<Comment>, listener: CacheUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            commentDao!!.deleteComments(comments)

            AppExecutors.getInstance().mainThread().execute {
                listener.onLocalCacheUpdateSuccess()
            }
        }
    }
}