package com.atmko.skiptoit.viewmodel

import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.viewmodel.paging.ParentCommentBoundaryCallback
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.viewmodel.common.CommentsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class ParentCommentsViewModel(
    googleSignInClient: GoogleSignInClient,
    skipToItApi: SkipToItApi,
    private var commentDao: CommentDao,
    private val parentCommentMediator: ParentCommentBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : CommentsViewModel(
    skipToItApi,
    googleSignInClient,
    commentDao,
    parentCommentMediator
) {

    fun getComments(episodeId: String) {
        if (retrievedComments != null
            && retrievedComments!!.value != null
            && !retrievedComments!!.value!!.isEmpty()
        ) {
            return
        }

        parentCommentMediator.param = episodeId
        val dataSourceFactory = commentDao.getAllComments(episodeId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Comment>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(parentCommentMediator)
        retrievedComments = pagedListBuilder.build()
    }
}