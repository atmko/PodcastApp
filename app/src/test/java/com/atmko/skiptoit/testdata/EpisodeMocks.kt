package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Episode

class EpisodeMocks {

    companion object {
        val EPISODE_ID: String = "episodeId"
        val TITLE: String = "title"
        val DESCRIPTION: String = "description"
        val IMAGE: String = "image"
        val AUDIO: String = "audio"
        val PUBLISH_DATE_1: Long = 1
        val LENTH_IN_SECONDS: Int = 3600

        val PODCAST_ID = "podcastId"

        fun GET_EPISODE(): Episode {
            return Episode(
                EPISODE_ID,
                TITLE,
                DESCRIPTION,
                IMAGE,
                AUDIO,
                PUBLISH_DATE_1,
                LENTH_IN_SECONDS
            )
        }

        fun GET_EPISODE_DETAILS(): Episode {
            val episode = GET_EPISODE()
            episode.podcastId = PODCAST_ID
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