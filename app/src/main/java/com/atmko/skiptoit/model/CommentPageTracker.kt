package com.atmko.skiptoit.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "comment_page_trackers",
    foreignKeys = [ForeignKey(
        entity = Comment::class,
        parentColumns = ["comment_id"],
        childColumns = ["comment_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
class CommentPageTracker(
    @PrimaryKey
    @ColumnInfo(name = "comment_id")
    val commentId: String,
    val page: Int,
    @ColumnInfo(name = "next_page")
    val nextPage: Int?,
    @ColumnInfo(name = "prev_page")
    val prevPage: Int?
)