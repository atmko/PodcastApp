package com.atmko.skiptoit.model.database

import androidx.room.*
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker

@Dao
interface CommentPageTrackerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<CommentPageTracker>)

    @Query("SELECT * FROM comment_page_trackers WHERE comment_id = :commentId")
    fun getCommentPageTracker(commentId: String): CommentPageTracker

    @Query("SELECT * FROM comments INNER JOIN comment_page_trackers ON comments.comment_id = comment_page_trackers.comment_id WHERE parent_id IS NULL AND episode_id = :episodeId AND page = :page")
    fun getCommentsInPage(episodeId: String, page: Int): List<Comment>

    @Query("SELECT * FROM comments INNER JOIN comment_page_trackers ON comments.comment_id = comment_page_trackers.comment_id WHERE parent_id = :parentId AND page = :page")
    fun getRepliesInPage(parentId: String, page: Int): List<Comment>
}