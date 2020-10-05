package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastDetails

class PodcastMocks {

    companion object {
        val PODCAST_ID: String = "podcastId"
        val TITLE: String = "title"
        val PUBLISHER: String = "publisher"
        val IMAGE: String = "image"
        val DESCRIPTION: String = "description"
        val TOTAL_EPISODES: Int = 0

        fun GET_PODCAST(): Podcast {
            return Podcast(
                PODCAST_ID,
                TITLE,
                PUBLISHER,
                IMAGE,
                DESCRIPTION,
                TOTAL_EPISODES
            )
        }
    }

    class PodcastDetailsMocks {
        companion object {
            val EPISODES: List<Episode> = listOf()
            val NEXT_EPISODE_PUBLISH_DATE: Long = 0

            fun GET_PODCAST_DETAILS(): PodcastDetails {
                return PodcastDetails(
                    PODCAST_ID,
                    TITLE,
                    PUBLISHER,
                    IMAGE,
                    DESCRIPTION,
                    TOTAL_EPISODES,
                    EPISODES,
                    NEXT_EPISODE_PUBLISH_DATE
                )
            }
        }
    }
}