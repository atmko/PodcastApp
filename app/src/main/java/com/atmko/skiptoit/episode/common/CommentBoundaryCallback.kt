package com.atmko.skiptoit.episode.common

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.viewmodel.common.BaseBoundaryCallback

open class CommentBoundaryCallback : BaseBoundaryCallback<Comment>() {

    companion object {
        const val loadTypeRefresh = 0
        const val loadTypeAppend = 1
        const val loadTypePrepend = -1
    }

    lateinit var param: String
    var startPage: Int = 1
}