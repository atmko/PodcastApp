package com.atmko.skiptoit.episode.replies

import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.episode.common.CommentsEndpoint
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.model.database.CommentDao

class RepliesViewModel(
    commentEndpoint: CommentsEndpoint,
    private val commentCache: CommentCache,
    private var commentDao: CommentDao,
    private val replyCommentBoundaryCallback: ReplyCommentBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : CommentsViewModel(
    commentEndpoint,
    commentCache,
    replyCommentBoundaryCallback
) {

    interface Listener {
        fun onParentCommentFetched(comment: Comment)
        fun onParentCommentFetchFailed()
        fun onVoteParentUpdate()
        fun onDeleteParentComment()
    }

    private val repliesViewModelListeners = mutableListOf<Listener>()
    lateinit var parentId: String

    fun getParentCommentAndNotify(parentCommentId: String) {
        parentId = parentCommentId
        commentCache.getCachedComment(parentCommentId, object : CommentCache.CommentFetchListener {
            override fun onCommentFetchSuccess(fetchedComment: Comment?) {
                if (fetchedComment != null) {
                    notifyParentCommentFetched(fetchedComment)
                } else {
                    notifyParentCommentFetchFailed()
                }
            }

            override fun onCommentFetchFailed() {
                notifyParentCommentFetchFailed()
            }
        })
    }

    fun getReplies(parentId: String) {
        if (retrievedComments != null
            && retrievedComments!!.value != null
            && !retrievedComments!!.value!!.isEmpty()
        ) {
            return
        }

        replyCommentBoundaryCallback.param = parentId
        // todo: use comment cache to get all replies instead of via commentDao
        val dataSourceFactory = commentDao.getAllReplies(parentId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Comment>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(replyCommentBoundaryCallback)
        retrievedComments = pagedListBuilder.build()
    }

    fun registerRepliesViewModelListener(listener: Listener) {
        repliesViewModelListeners.add(listener)
    }

    fun unregisterRepliesViewModelListener(listener: Listener) {
        repliesViewModelListeners.remove(listener)
    }

    private fun unregisterRepliesViewModelListeners() {
        // todo: use clear method in other similar view model methods
        repliesViewModelListeners.clear()
    }

    private fun notifyParentCommentFetched(parentComment: Comment) {
        for (listener in repliesViewModelListeners) {
            listener.onParentCommentFetched(parentComment)
        }
    }

    private fun notifyParentCommentFetchFailed() {
        for (listener in repliesViewModelListeners) {
            listener.onParentCommentFetchFailed()
        }
    }

    override fun notifyVoteSuccess(commentId: String) {
        if (commentId != parentId) {
            super.notifyVoteSuccess(commentId)
        } else {
            for (listener in repliesViewModelListeners) {
                listener.onVoteParentUpdate()
            }
        }
    }

    override fun notifyDeleteSuccess(commentId: String) {
        if (commentId != parentId) {
            super.notifyDeleteSuccess(commentId)
        } else {
            for (listener in repliesViewModelListeners) {
                listener.onDeleteParentComment()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterRepliesViewModelListeners()
    }
}