package com.atmko.skiptoit.viewmodel.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.database.SkipToItDatabase

open class CommentBoundaryCallback(
    private val skipToItDatabase: SkipToItDatabase
): PagedList.BoundaryCallback<Comment>() {

    val loadTypeRefresh = 0
    val loadTypeAppend = 1
    val loadTypePrepend = -1

    lateinit var param: String
    var startPage: Int = 1

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()

    fun getCommentPageTracker(commentId: String): CommentPageTracker {
        return skipToItDatabase.commentPageTrackerDao().getCommentPageTracker(commentId)
    }
}