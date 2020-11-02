package com.atmko.skiptoit.episodelist

import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.EpisodesCache

class EpisodeBoundaryCallback(
    private val getEpisodesEndpoint: GetEpisodesEndpoint,
    private val episodesCache: EpisodesCache
): BaseBoundaryCallback<Episode>() {

    private val loadTypeRefresh = 0
    private val loadTypeAppend = 1

    lateinit var param: String
    var startPage: Long? = null
    
    override fun onZeroItemsLoaded() {
        notifyPageLoading()
        requestPage(loadTypeRefresh, startPage)
    }

    override fun onItemAtEndLoaded(itemAtEnd: Episode) {
        notifyPageLoading()
        requestPage(loadTypeAppend, itemAtEnd.publishDate)
    }

    override fun onItemAtFrontLoaded(itemAtFront: Episode) {

    }

    private fun requestPage(loadType: Int, loadKey: Long?) {
        getEpisodesEndpoint.getEpisodes(param, loadKey, object : GetEpisodesEndpoint.Listener {
            override fun onEpisodesQuerySuccess(podcastDetails: PodcastDetails) {
                episodesCache.insertEpisodesForPaging(
                    podcastDetails, loadType, param, object : EpisodesCache.PageFetchListener {
                        override fun onPageFetchSuccess() {
                            notifyOnPageLoad()
                        }

                        override fun onPageFetchFailed() {
                            notifyOnPageLoadFailed()
                        }
                    })
            }

            override fun onEpisodesQueryFailed() {
                notifyOnPageLoadFailed()
            }
        })
    }
}