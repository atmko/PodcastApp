package com.atmko.skiptoit.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val BODY_KEY: String = "body"
const val USERNAME_KEY = "username"

const val VOTE_WEIGHT_NONE: Int = 0
const val VOTE_WEIGHT_UP_VOTE: Int = 1
const val VOTE_WEIGHT_DOWN_VOTE: Int = -1

const val VOTE_WEIGHT_UP_VOTE_INVERT: Int = -2
const val VOTE_WEIGHT_DOWN_VOTE_INVERT: Int = 2

const val VOTE_WEIGHT_NEUTRALIZE_UP_VOTE: Int = -1
const val VOTE_WEIGHT_NEUTRALIZE_DOWN_VOTE: Int = 1

@Entity(tableName = "comments")
class Comment(
    @PrimaryKey
    @ColumnInfo(name = "comment_id")
    @SerializedName("comment_id")
    val commentId: String,
    @ColumnInfo(name = "parent_id")
    @SerializedName("parent_id")
    val parentId: String?,
    @ColumnInfo(name = "episode_id")
    @SerializedName("episode_id")
    val episodeId: String,
    val username: String,
    var body: String,
    @ColumnInfo(name = "vote_tally")
    @SerializedName("vote_tally")
    var voteTally: Int,
    @ColumnInfo(name = "is_user_comment")
    @SerializedName("is_user_comment")
    val isUserComment: Boolean,
    @ColumnInfo(name = "vote_weight")
    @SerializedName("vote_weight")
    var voteWeight: Int,
    val replies: Int,
    val timestamp: Long,
    @ColumnInfo(name = "profile_image")
    @SerializedName("profile_image")
    val profileImage: String?) : Serializable {

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        if (commentId != other.commentId) return false
        if (parentId != other.parentId) return false
        if (episodeId != other.episodeId) return false
        if (username != other.username) return false
        if (body != other.body) return false
        if (voteTally != other.voteTally) return false
        if (isUserComment != other.isUserComment) return false
        if (voteWeight != other.voteWeight) return false
        if (replies != other.replies) return false
        if (timestamp != other.timestamp) return false
        if (profileImage != other.profileImage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commentId.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + episodeId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + voteTally
        result = 31 * result + isUserComment.hashCode()
        result = 31 * result + voteWeight
        result = 31 * result + replies
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (profileImage?.hashCode() ?: 0)
        return result
    }
}