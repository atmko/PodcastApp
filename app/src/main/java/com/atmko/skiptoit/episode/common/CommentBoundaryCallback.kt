package com.atmko.skiptoit.episode.common

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.viewmodel.common.BaseBoundaryCallback

open class CommentBoundaryCallback(
    private val skipToItDatabase: SkipToItDatabase,
    protected val listener: Listener
) : BaseBoundaryCallback<Comment>() {

    val loadTypeRefresh = 0
    val loadTypeAppend = 1
    val loadTypePrepend = -1

    lateinit var param: String
    var startPage: Int = 1

    protected fun getCommentPageTracker(commentId: String): CommentPageTracker {
        return skipToItDatabase.commentPageTrackerDao().getCommentPageTracker(commentId)
    }
}