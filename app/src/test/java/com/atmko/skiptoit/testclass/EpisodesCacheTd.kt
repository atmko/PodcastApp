package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.EpisodesCache

class EpisodesCacheTd : EpisodesCache(null, null) {

    var mFailure = false
    lateinit var mPodcastDetails: PodcastDetails
    var mLoadType: Int? = null
    var mParam: String = ""

    override fun insertEpisodes(
        podcastDetails: PodcastDetails,
        loadType: Int,
        param: String,
        listener: PageFetchListener
    ) {
        mPodcastDetails = podcastDetails
        mLoadType = loadType
        mParam = param
        if (!mFailure) {
            listener.onPageFetchSuccess()
        } else {
            listener.onPageFetchFailed()
        }
    }
}
