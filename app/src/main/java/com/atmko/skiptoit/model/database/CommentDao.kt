package com.atmko.skiptoit.model.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.atmko.skiptoit.model.Comment

@Dao
interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComments(comments: List<Comment>)

    @Query("SELECT * FROM comments WHERE comment_id = :commentId")
    fun getComment(commentId: String): Comment

    @Query("SELECT * FROM comments WHERE parent_id IS NULL AND episode_id = :episodeId ORDER BY timestamp DESC LIMIT 1")
    fun getLastComment(episodeId: String): Comment?

    @Query("SELECT * FROM comments WHERE parent_id = :parentId ORDER BY timestamp DESC LIMIT 1")
    fun getLastReply(parentId: String): Comment?

    @Query("SELECT * FROM comments WHERE parent_id IS NULL AND episode_id = :episodeId ORDER BY timestamp")
    fun getAllComments(episodeId: String):  DataSource.Factory<Int, Comment>

    @Query("SELECT * FROM comments WHERE parent_id = :parentId ORDER BY timestamp")
    fun getAllReplies(parentId: String) : DataSource.Factory<Int, Comment>

    @Update
    fun updateComment(comment: Comment)

    @Delete
    fun deleteComments(comments: List<Comment>)

    @Query("DELETE FROM comments")
    fun deleteAllComments()

    @Query("DELETE FROM comments WHERE parent_id = :parentId")
    fun deleteAllReplies(parentId: String)
}