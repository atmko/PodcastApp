package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class CommentResults(
    @SerializedName("comments")
    val comments: List<Comment>,
    val page: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("has_prev")
    val hasPrev: Boolean)