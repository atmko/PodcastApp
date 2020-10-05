package com.atmko.skiptoit.episode.common

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.common.BaseBoundaryCallback

open class CommentBoundaryCallback : BaseBoundaryCallback<Comment>() {

    lateinit var param: String
    var startPage: Int = 1
}