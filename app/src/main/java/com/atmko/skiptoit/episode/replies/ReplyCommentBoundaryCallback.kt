package com.atmko.skiptoit.episode.replies

import com.atmko.skiptoit.episode.common.CommentBoundaryCallback
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.model.database.CommentCache.CommentPageTrackerListener

class ReplyCommentBoundaryCallback(
    private val getRepliesEndpoint: GetRepliesEndpoint,
    private val commentCache: CommentCache
) : CommentBoundaryCallback() {

    override fun onZeroItemsLoaded() {
        notifyPageLoading()
        requestPage(loadTypeRefresh, startPage)
    }

    override fun onItemAtEndLoaded(itemAtEnd: Comment) {
        notifyPageLoading()
        commentCache.getPageTrackers(itemAtEnd.commentId, object : CommentPageTrackerListener {
            override fun onPageTrackerFetched(commentPageTracker: CommentPageTracker) {
                val nextPage = commentPageTracker.nextPage
                if (nextPage != null) {
                    requestPage(loadTypeAppend, nextPage)
                } else {
                    notifyOnPageLoad()
                }
            }

            override fun onPageTrackerFetchFailed() {
                notifyOnPageLoadFailed()
            }
        })
    }

    override fun onItemAtFrontLoaded(itemAtFront: Comment) {
        notifyPageLoading()
        commentCache.getPageTrackers(itemAtFront.commentId, object : CommentPageTrackerListener {
            override fun onPageTrackerFetched(commentPageTracker: CommentPageTracker) {
                val prevPage = commentPageTracker.prevPage
                if (prevPage != null) {
                    requestPage(loadTypePrepend, prevPage)
                } else {
                    notifyOnPageLoad()
                }
            }

            override fun onPageTrackerFetchFailed() {
                notifyOnPageLoadFailed()
            }
        })
    }

    private fun requestPage(loadType: Int, loadKey: Int) {
        getRepliesEndpoint.getReplies(param, loadKey, object : GetRepliesEndpoint.Listener {
            override fun onQuerySuccess(commentResults: CommentResults) {
                commentCache.insertRepliesAndTrackers(
                    commentResults, loadType, param, object : CommentCache.PageFetchListener {
                        override fun onPageFetchSuccess() {
                            notifyOnPageLoad()
                        }

                        override fun onPageFetchFailed() {
                            notifyOnPageLoadFailed()
                        }
                    }
                )
            }

            override fun onQueryFailed() {
                notifyOnPageLoadFailed()
            }
        })
    }
}