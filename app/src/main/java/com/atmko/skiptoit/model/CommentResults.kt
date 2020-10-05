package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class CommentResults(
    @SerializedName("comments")
    val comments: List<Comment>,
    val page: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("has_prev")
    val hasPrev: Boolean) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommentResults

        if (comments != other.comments) return false
        if (page != other.page) return false
        if (hasNext != other.hasNext) return false
        if (hasPrev != other.hasPrev) return false

        return true
    }

    override fun hashCode(): Int {
        var result = comments.hashCode()
        result = 31 * result + page
        result = 31 * result + hasNext.hashCode()
        result = 31 * result + hasPrev.hashCode()
        return result
    }
}