package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.testdata.CommentMocks

class CommentCacheTd : CommentCache(null) {

    var mCommentId: String = ""
    var mGetCachedCommentCounter = 0
    var mFailure = false

    override
    fun getCachedComment(commentId: String, listener: CommentFetchListener) {
        mCommentId = commentId
        mGetCachedCommentCounter += 1

        if (!mFailure) {
            if (mGetCachedCommentCounter == 1) {
                listener.onCommentFetchSuccess(CommentMocks.GET_COMMENT_1())
            } else if (mGetCachedCommentCounter == 2) {
                listener.onCommentFetchSuccess(CommentMocks.GET_COMMENT_2())
            }
        }
    }

    var mUpdateLocalCacheCounter = 0
    var mUpdatedComment: Comment = CommentMocks.GET_COMMENT_1()

    override fun updateLocalCache(updatedComment: Comment, listener: CacheUpdateListener) {
        mUpdateLocalCacheCounter += 1
        mUpdatedComment = updatedComment

        if (!mFailure) {
            listener.onLocalCacheUpdateSuccess()
        }
    }

    var mDeleteCommentsCounter = 0
    lateinit var mComments: List<Comment>

    override fun deleteComments(comments: List<Comment>, listener: CacheUpdateListener) {
        mDeleteCommentsCounter += 1
        mComments = comments

        if (!mFailure) {
            listener.onLocalCacheUpdateSuccess()
        }
    }
}
