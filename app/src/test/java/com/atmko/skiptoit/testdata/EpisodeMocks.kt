package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Episode

class EpisodeMocks {

    companion object {
        val EPISODE_ID: String = "episodeId"
        val TITLE: String = "title"
        val DESCRIPTION: String = "description"
        val IMAGE: String = "image"
        val AUDIO: String = "audio"
        val PUBLISH_DATE: Long = 0
        val LENTH_IN_SECONDS: Int = 3600

        fun GET_EPISODE(): Episode {
            return Episode(
                EPISODE_ID,
                TITLE,
                DESCRIPTION,
                IMAGE,
                AUDIO,
                PUBLISH_DATE,
                LENTH_IN_SECONDS
            )
        }
    }
}