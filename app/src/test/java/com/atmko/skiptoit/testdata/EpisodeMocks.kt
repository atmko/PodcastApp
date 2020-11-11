package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Episode

class EpisodeMocks {

    companion object {
        val EPISODE_ID_1: String = "episodeId1"
        val EPISODE_ID_2: String = "episodeId2"
        val TITLE: String = "title"
        val DESCRIPTION: String = "description"
        val IMAGE: String = "image"
        val AUDIO: String = "audio"
        val PUBLISH_DATE_1: Long = 1
        val LENTH_IN_SECONDS: Int = 3600

        val PODCAST_ID = "podcastId"
        val LAST_PLAYBACK_POSITION: Long = 1000

        fun GET_EPISODE_1(): Episode {
            return Episode(
                EPISODE_ID_1,
                TITLE,
                DESCRIPTION,
                IMAGE,
                AUDIO,
                PUBLISH_DATE_1,
                LENTH_IN_SECONDS
            )
        }

        fun GET_EPISODE_2(): Episode {
            return Episode(
                EPISODE_ID_2,
                TITLE,
                DESCRIPTION,
                IMAGE,
                AUDIO,
                PUBLISH_DATE_1,
                LENTH_IN_SECONDS
            )
        }

        fun GET_EPISODE_DETAILS(): Episode {
            val episode = GET_EPISODE_1()
            episode.podcastId = PODCAST_ID
            episode.lastPlaybackPosition = LAST_PLAYBACK_POSITION
            return episode
        }

        val NEXT_EPISODE_ID: String = "nextEpisodeId"
        val PUBLISH_DATE_2: Long = 2
        fun GET_NEXT_EPISODE(): Episode {
            return Episode(
                NEXT_EPISODE_ID,
                TITLE,
                DESCRIPTION,
                IMAGE,
                AUDIO,
                PUBLISH_DATE_2,
                LENTH_IN_SECONDS
            )
        }

        val PREV_EPISODE_ID: String = "prevEpisodeId"
        val PUBLISH_DATE_0: Long = 0
        fun GET_PREV_EPISODE(): Episode {
            return Episode(
                PREV_EPISODE_ID,
                TITLE,
                DESCRIPTION,
                IMAGE,
                AUDIO,
                PUBLISH_DATE_0,
                LENTH_IN_SECONDS
            )
        }
    }
}