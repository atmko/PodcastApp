package com.atmko.skiptoit.model.database

import androidx.paging.DataSource
import androidx.room.*
import com.atmko.skiptoit.model.Episode

@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisodes(episodes: List<Episode>)

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId ORDER BY publish_date DESC")
    fun getAllEpisodes(podcastId: String):  DataSource.Factory<Int, Episode>

    @Query("DELETE FROM episodes")
    fun deleteAllEpisodes()
}