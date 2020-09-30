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
    commentCache: CommentCache,
    private var commentDao: CommentDao,
    private val replyCommentBoundaryCallback: ReplyCommentBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : CommentsViewModel(
    commentEndpoint,
    commentCache
) {

    fun getReplies(parentId: String) {
        if (retrievedComments != null
            && retrievedComments!!.value != null
            && !retrievedComments!!.value!!.isEmpty()
        ) {
            return
        }

        replyCommentBoundaryCallback.param = parentId
        val dataSourceFactory = commentDao.getAllReplies(parentId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Comment>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(replyCommentBoundaryCallback)
        retrievedComments = pagedListBuilder.build()
    }
}