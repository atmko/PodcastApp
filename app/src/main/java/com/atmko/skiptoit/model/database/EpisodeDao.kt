package com.atmko.skiptoit.model.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.atmko.skiptoit.model.Episode

@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisodes(episodes: List<Episode>)

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId ORDER BY publish_date DESC")
    fun getAllEpisodesForPodcast(podcastId: String):  DataSource.Factory<Int, Episode>

    //deletes all episodes except episode from podcast last played
    @Query("DELETE FROM episodes WHERE podcast_id <> :nowPlayingPodcastId")
    fun deleteAllEpisodesExceptNowPlaying(nowPlayingPodcastId: String)

    @Query("DELETE FROM episodes WHERE podcast_id = :podcastId")
    fun deletePodcastEpisodes(podcastId: String)

    @Query("SELECT * FROM episodes WHERE publish_date >= :currentEpisodePublishDate AND episode_id <> :currentEpisodeId ORDER BY publish_date ASC LIMIT 1")
    fun getNextEpisode(currentEpisodeId: String, currentEpisodePublishDate: Long): Episode?

    @Query("SELECT * FROM episodes WHERE publish_date <= :currentEpisodePublishDate AND episode_id <> :currentEpisodeId ORDER BY publish_date DESC LIMIT 1")
    fun getPrevEpisode(currentEpisodeId: String, currentEpisodePublishDate: Long): Episode?
}