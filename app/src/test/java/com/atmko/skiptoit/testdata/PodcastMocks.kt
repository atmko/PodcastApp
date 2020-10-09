package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastDetails

class PodcastMocks {

    companion object {
        val PODCAST_ID_1: String = "podcastId1"
        val PODCAST_ID_2: String = "podcastId2"
        val TITLE: String = "title"
        val PUBLISHER: String = "publisher"
        val IMAGE: String = "image"
        val DESCRIPTION: String = "description"
        val TOTAL_EPISODES: Int = 0

        fun GET_PODCAST_1(): Podcast {
            return Podcast(
                PODCAST_ID_1,
                TITLE,
                PUBLISHER,
                IMAGE,
                DESCRIPTION,
                TOTAL_EPISODES
            )
        }

        fun GET_PODCAST_2(): Podcast {
            return Podcast(
                PODCAST_ID_2,
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
            val WITHOUT_EPISODES: List<Episode> = listOf()
            val NEXT_EPISODE_PUBLISH_DATE: Long = 0

            fun GET_PODCAST_DETAILS_WITHOUT_EPISODES(): PodcastDetails {
                return PodcastDetails(
                    PODCAST_ID_1,
                    TITLE,
                    PUBLISHER,
                    IMAGE,
                    DESCRIPTION,
                    TOTAL_EPISODES,
                    WITHOUT_EPISODES,
                    NEXT_EPISODE_PUBLISH_DATE
                )
            }

            val WITH_EPISODES: List<Episode> = listOf(EpisodeMocks.GET_NEXT_EPISODE())
            fun GET_PODCAST_DETAILS_WITH_EPISODES(): PodcastDetails {
                return PodcastDetails(
                    PODCAST_ID_1,
                    TITLE,
                    PUBLISHER,
                    IMAGE,
                    DESCRIPTION,
                    TOTAL_EPISODES,
                    WITH_EPISODES,
                    NEXT_EPISODE_PUBLISH_DATE
                )
            }
        }
    }
}