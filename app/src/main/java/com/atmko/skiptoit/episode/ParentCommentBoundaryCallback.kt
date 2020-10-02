package com.atmko.skiptoit.episode

import android.util.Log
import com.atmko.skiptoit.episode.common.CommentBoundaryCallback
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.model.database.CommentCache.CommentPageTrackerListener

class ParentCommentBoundaryCallback(
    private val getCommentsEndpoint: GetCommentsEndpoint,
    private val commentCache: CommentCache
) : CommentBoundaryCallback() {

    private val tag = this::class.simpleName

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
        getCommentsEndpoint.getComments(param, loadKey, object : GetCommentsEndpoint.Listener {
            override fun onQuerySuccess(commentResults: CommentResults) {
                commentCache.insertCommentsAndTrackers(
                    commentResults, loadType, object : CommentCache.PageFetchListener {
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