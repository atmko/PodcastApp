package com.atmko.skiptoit.viewmodel

import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.viewmodel.paging.ReplyCommentBoundaryCallback
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.viewmodel.common.CommentsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class RepliesViewModel(
    googleSignInClient: GoogleSignInClient,
    skipToItApi: SkipToItApi,
    private var commentDao: CommentDao,
    private val replyCommentMediator: ReplyCommentBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : CommentsViewModel(
    skipToItApi,
    googleSignInClient,
    commentDao,
    replyCommentMediator
) {

    fun getReplies(parentId: String) {
        if (retrievedComments != null
            && retrievedComments!!.value != null
            && !retrievedComments!!.value!!.isEmpty()
        ) {
            return
        }

        replyCommentMediator.param = parentId
        val dataSourceFactory = commentDao.getAllReplies(parentId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Comment>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(replyCommentMediator)
        retrievedComments = pagedListBuilder.build()
    }
}