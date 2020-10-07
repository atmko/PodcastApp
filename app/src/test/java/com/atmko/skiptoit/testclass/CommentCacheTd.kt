package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.testdata.CommentMocks
import com.atmko.skiptoit.testdata.CommentPageTrackerMocks

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

    var mComment: Comment? = null
    override fun updateCommentPagingTracker(comment: Comment, listener: UpdatePagingDataListener) {
        mComment = comment
        if (!mFailure) {
            listener.onPagingDataUpdated(comment)
        }
    }

    override fun updateReplyPagingTracker(reply: Comment, listener: UpdatePagingDataListener) {
        mComment = reply
        if (!mFailure) {
            listener.onPagingDataUpdated(reply)
        }
    }

    var mNextPage = false
    var mPrevPage = false
    override fun getPageTrackers(commentId: String, listener: CommentPageTrackerListener) {
        mCommentId = commentId
        if (!mFailure) {
            if (mNextPage) {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NEXT_PAGE())
            } else if (mPrevPage) {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_PREV_PAGE())
            } else {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NULL_NEXT_PAGE())
            }
        } else {
            listener.onPageTrackerFetchFailed()
        }
    }

    lateinit var mCommentResults: CommentResults
    var mLoadType: Int? = null
    var mParam: String = ""

    override fun insertCommentsAndTrackers(
        commentResults: CommentResults,
        loadType: Int,
        listener: PageFetchListener
    ) {
        mCommentResults = commentResults
        mLoadType = loadType
        if (!mFailure) {
            listener.onPageFetchSuccess()
        } else {
            listener.onPageFetchFailed()
        }
    }

    override fun insertRepliesAndTrackers(
        commentResults: CommentResults,
        loadType: Int,
        param: String,
        listener: PageFetchListener
    ) {
        mCommentResults = commentResults
        mLoadType = loadType
        mParam = param
        if (!mFailure) {
            listener.onPageFetchSuccess()
        } else {
            listener.onPageFetchFailed()
        }
    }
}
