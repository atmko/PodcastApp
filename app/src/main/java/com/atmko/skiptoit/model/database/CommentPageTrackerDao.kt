package com.atmko.skiptoit.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atmko.skiptoit.model.CommentPageTracker

@Dao
interface CommentPageTrackerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<CommentPageTracker>)

    @Query("SELECT * FROM comment_page_trackers WHERE comment_id = :commentId")
    fun getCommentPageTracker(commentId: String): CommentPageTracker

    @Query("SELECT * FROM (SELECT * FROM comments INNER JOIN comment_page_trackers ON comments.comment_id = comment_page_trackers.comment_id WHERE parent_id IS NULL AND episode_id = :episodeId ORDER BY timestamp DESC LIMIT 1)")
    fun getLastCommentPageTracker(episodeId: String): CommentPageTracker?

    @Query("SELECT * FROM (SELECT * FROM comments INNER JOIN comment_page_trackers ON comments.comment_id = comment_page_trackers.comment_id WHERE parent_id = :parentId ORDER BY timestamp DESC LIMIT 1)")
    fun getLastReplyPageTracker(parentId: String): CommentPageTracker?
}