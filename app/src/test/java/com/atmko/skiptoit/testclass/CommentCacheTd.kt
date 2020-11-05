package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.testdata.CommentMocks
import com.atmko.skiptoit.testdata.CommentPageTrackerMocks

class CommentCacheTd : CommentCache(null) {

    var mCommentId: String = ""
    var mFailure = false

    var mGetCachedCommentCounter = 0
    lateinit var mGetCachedCommentArgCommentId: String
    var mGetCachedCommentFailure = false
    var mGetCachedCommentNullCommentReturned = true
    override fun getCachedComment(commentId: String, listener: CommentFetchListener) {
        mGetCachedCommentCounter++
        mGetCachedCommentArgCommentId = commentId
        mCommentId = commentId

        if (mGetCachedCommentFailure) {
            listener.onCommentFetchFailed()
            return
        }

        if (mGetCachedCommentNullCommentReturned) {
            listener.onCommentFetchSuccess(null)
        } else {
            listener.onCommentFetchSuccess(CommentMocks.GET_COMMENT_1())
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

    var mGetLastCommentPageTrackerCounter = 0
    var mGetLastCommentPageTrackerFailure = false
    lateinit var mGetLastCommentPageTrackerArgEpisodeId: String
    var mGetLastCommentPageTrackerNullPageTracker = true
    var mGetLastCommentPageTrackerNoNextPage = false
    override fun getLastCommentPageTracker(
        episodeId: String,
        listener: CommentPageTrackerListener
    ) {
        mGetLastCommentPageTrackerCounter++
        mGetLastCommentPageTrackerArgEpisodeId = episodeId
        if (mGetLastCommentPageTrackerFailure) {
            listener.onPageTrackerFetchFailed()
            return
        }
        if (!mGetLastCommentPageTrackerNullPageTracker) {
            if (!mGetLastCommentPageTrackerNoNextPage) {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NEXT_PAGE())
            } else {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NULL_NEXT_PAGE())
            }
        } else {
            listener.onPageTrackerFetched(null)
        }
    }

    var mDeleteAllCommentsInPageCounter = 0
    lateinit var mDeleteAllCommentsInPageArgEpisodeId: String
    var mDeleteAllCommentsInPageArgPage: Int? = null
    var mDeleteAllCommentsInPageFailure = false
    override fun deleteAllCommentsInPage(
        episodeId: String,
        page: Int,
        listener: DeletePageListener
    ) {
        mDeleteAllCommentsInPageCounter++
        mDeleteAllCommentsInPageArgEpisodeId = episodeId
        mDeleteAllCommentsInPageArgPage = page
        if (!mDeleteAllCommentsInPageFailure) {
            listener.onPageDeleted()
        } else {
            listener.onPageDeleteFailed()
        }
    }

    var mGetLastReplyPageTrackerCounter = 0
    lateinit var mGetLastReplyPageTrackerArgParentId: String
    var mGetLastReplyPageTrackerFailure = false
    var mGetLastReplyPageTrackerNullPageTracker = true
    var mGetLastReplyPageTrackerNoNextPage = false
    override fun getLastReplyPageTracker(parentId: String, listener: CommentPageTrackerListener) {
        mGetLastReplyPageTrackerCounter++
        mGetLastReplyPageTrackerArgParentId = parentId
        if (mGetLastReplyPageTrackerFailure) {
            listener.onPageTrackerFetchFailed()
            return
        }
        if (!mGetLastReplyPageTrackerNullPageTracker) {
            if (!mGetLastReplyPageTrackerNoNextPage) {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NEXT_PAGE())
            } else {
                listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NULL_NEXT_PAGE())
            }
        } else {
            listener.onPageTrackerFetched(null)
        }
    }

    var mDeleteAllRepliesInPageCounter = 0
    lateinit var mDeleteAllRepliesInPageArgEpisodeId: String
    var mDeleteAllRepliesInPageArgPage: Int? = null
    var mDeleteAllRepliesInPageFailure = false
    override fun deleteAllRepliesInPage(
        parentId: String,
        page: Int,
        listener: DeletePageListener
    ) {
        mDeleteAllRepliesInPageCounter++
        mDeleteAllRepliesInPageArgEpisodeId = parentId
        mDeleteAllRepliesInPageArgPage = page
        if (!mDeleteAllRepliesInPageFailure) {
            listener.onPageDeleted()
        } else {
            listener.onPageDeleteFailed()
        }
    }

    var mIncreaseReplyCountCounter = 0
    lateinit var mIncreaseReplyCountArgCommentId: String
    var mIncreaseReplyCountFailure = false
    override fun increaseReplyCount(commentId: String, listener: UpdateReplyCountListener) {
        mIncreaseReplyCountCounter++
        mIncreaseReplyCountArgCommentId = commentId
        if (!mIncreaseReplyCountFailure) {
            listener.onReplyCountUpdated()
        } else {
            listener.onReplyCountUpdateFailed()
        }
    }

    var mDecreaseReplyCountCounter = 0
    lateinit var mDecreaseReplyCountArgCommentId: String
    var mDecreaseReplyCountFailure = false
    override fun decreaseReplyCount(commentId: String, listener: UpdateReplyCountListener) {
        mDecreaseReplyCountCounter++
        mDecreaseReplyCountArgCommentId = commentId
        if (!mDecreaseReplyCountFailure) {
            listener.onReplyCountUpdated()
        } else {
            listener.onReplyCountUpdateFailed()
        }
    }

    var mNextPage = false
    var mPrevPage = false
    override fun getPageTracker(commentId: String, listener: CommentPageTrackerListener) {
        mCommentId = commentId
        if (!mFailure) {
            when {
                mNextPage -> {
                    listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NEXT_PAGE())
                }
                mPrevPage -> {
                    listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_PREV_PAGE())
                }
                else -> {
                    listener.onPageTrackerFetched(CommentPageTrackerMocks.COMMENT_PAGE_TRACKER_NULL_NEXT_PAGE())
                }
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
