package com.atmko.skiptoit.model.database

import androidx.paging.DataSource
import androidx.room.*
import com.atmko.skiptoit.model.Comment

@Dao
interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComments(comments: List<Comment>)

    @Query("SELECT * FROM comments WHERE comment_id = :commentId")
    fun getComment(commentId: String): Comment

    @Query("SELECT * FROM comments WHERE parent_id IS NULL AND episode_id = :episodeId ORDER BY timestamp")
    fun getAllComments(episodeId: String):  DataSource.Factory<Int, Comment>

    @Query("SELECT * FROM comments WHERE parent_id = :parentId ORDER BY timestamp")
    fun getAllReplies(parentId: String) : DataSource.Factory<Int, Comment>

    @Update
    fun updateComment(comment: Comment)

    @Query("UPDATE comments SET replies = replies + 1 WHERE comment_id = :commentId")
    fun increaseReplyCount(commentId: String)

    @Query("UPDATE comments SET replies = replies - 1 WHERE comment_id = :commentId")
    fun decreaseReplyCount(commentId: String)

    @Delete
    fun deleteComments(comments: List<Comment>)

    @Query("DELETE FROM comments")
    fun deleteAllComments()

    @Query("DELETE FROM comments WHERE parent_id = :parentId")
    fun deleteAllReplies(parentId: String)

    @Query("DELETE FROM comments WHERE comment_id IN (SELECT comments.comment_id FROM comments INNER JOIN comment_page_trackers ON comments.comment_id = comment_page_trackers.comment_id WHERE parent_id IS NULL AND episode_id = :episodeId AND page = :page)")
    fun deleteAllCommentsInPage(episodeId: String, page: Int)

    @Query("DELETE FROM comments WHERE comment_id IN (SELECT comments.comment_id FROM comments INNER JOIN comment_page_trackers ON comments.comment_id = comment_page_trackers.comment_id WHERE parent_id = :parentId AND page = :page)")
    fun deleteAllRepliesInPage(parentId: String, page: Int)
}