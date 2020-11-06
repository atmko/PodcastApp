package com.atmko.skiptoit.model.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atmko.skiptoit.model.Episode

@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisodes(episodes: List<Episode>)

    @Query("SELECT * FROM episodes WHERE episode_id = :episodeId")
    fun getEpisode(episodeId: String): Episode?

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId ORDER BY publish_date DESC")
    fun getAllPodcastEpisodes(podcastId: String):  DataSource.Factory<Int, Episode>

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId ORDER BY publish_date DESC")
    fun getAllPodcastEpisodesAlt(podcastId: String):  List<Episode>

    //deletes all episodes except episode from podcast last played
    @Query("DELETE FROM episodes WHERE podcast_id <> :nowPlayingPodcastId")
    fun deleteAllEpisodesExceptNowPlaying(nowPlayingPodcastId: String)

    @Query("DELETE FROM episodes WHERE podcast_id = :podcastId")
    fun deletePodcastEpisodes(podcastId: String)

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId AND publish_date >= :currentEpisodePublishDate AND episode_id <> :currentEpisodeId ORDER BY publish_date ASC LIMIT 1")
    fun getNextEpisode(podcastId: String, currentEpisodeId: String, currentEpisodePublishDate: Long): Episode?

    @Query("SELECT * FROM episodes WHERE podcast_id = :podcastId AND publish_date <= :currentEpisodePublishDate AND episode_id <> :currentEpisodeId ORDER BY publish_date DESC LIMIT 1")
    fun getPrevEpisode(podcastId: String, currentEpisodeId: String, currentEpisodePublishDate: Long): Episode?
}