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
        if (other !is Comment) return false
        return commentId == other.commentId
                && parentId == other.parentId
                && username == other.username
                && body == other.body
                && voteTally == other.voteTally
                && isUserComment == other.isUserComment
                && voteWeight == other.voteWeight
                && replies == other.replies
                && profileImage == other.profileImage
    }
}