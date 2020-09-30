package com.atmko.skiptoit.episode

import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.episode.common.CommentsEndpoint
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.model.database.CommentDao

class ParentCommentsViewModel(
    commentEndpoint: CommentsEndpoint,
    commentCache: CommentCache,
    private var commentDao: CommentDao,
    private val parentCommentBoundaryCallback: ParentCommentBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : CommentsViewModel(
    commentEndpoint,
    commentCache
) {

    fun getComments(episodeId: String) {
        if (retrievedComments != null
            && retrievedComments!!.value != null
            && !retrievedComments!!.value!!.isEmpty()
        ) {
            return
        }

        parentCommentBoundaryCallback.param = episodeId
        val dataSourceFactory = commentDao.getAllComments(episodeId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Comment>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(parentCommentBoundaryCallback)
        retrievedComments = pagedListBuilder.build()
    }
}