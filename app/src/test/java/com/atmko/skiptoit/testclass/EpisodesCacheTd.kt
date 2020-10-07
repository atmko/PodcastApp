package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.EpisodesCache
import com.atmko.skiptoit.testdata.EpisodeMocks

class EpisodesCacheTd : EpisodesCache(null, null) {

    var mFailure = false
    lateinit var mPodcastDetails: PodcastDetails
    var mLoadType: Int? = null
    var mParam: String = ""

    override fun insertEpisodesForPaging(
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

    var mPodcastId = ""
    override fun deletePodcastEpisodes(currentPodcastId: String, listener: DeletePodcastEpisodesListener) {
        mPodcastId = currentPodcastId
        if (!mFailure) {
            listener.onDeletePodcastEpisodesSuccess()
        } else {
            listener.onDeletePodcastEpisodesFailed()
        }
    }

    var mRestoreEpisodeCounter = 0
    override fun restoreEpisode(listener: RestoreEpisodeListener) {
        mRestoreEpisodeCounter += 1
        if (!mFailure) {
            listener.onEpisodeRestoreSuccess(EpisodeMocks.GET_EPISODE())
        } else {
            listener.onEpisodeRestoreFailed()
        }
    }

    var mGetNextEpisodeCounter = 0
    var mCacheQueryError = false
    var mEpisodeNotInCache = false
    var mEpisodeId = ""
    var mPublishDate: Long? = null
    override fun getNextEpisode(
        episodeId: String,
        publishDate: Long,
        listener: NextEpisodeListener
    ) {
        mGetNextEpisodeCounter += 1
        mEpisodeId = episodeId
        mPublishDate = publishDate
        if (!mCacheQueryError) {
            if (!mEpisodeNotInCache) {
                listener.onNextEpisodeFetchSuccess(EpisodeMocks.GET_NEXT_EPISODE())
            } else {
                listener.onNextEpisodeFetchSuccess(null)
            }
        } else {
            listener.onNextEpisodeFetchFailed()
        }
    }

    var mInsertEpisodesAndReturnNextEpisodeCounter = 0
    var mCacheWriteError = false
    override fun insertEpisodesAndReturnNextEpisode(
        podcastDetails: PodcastDetails,
        listener: NextEpisodeListener
    ) {
        mInsertEpisodesAndReturnNextEpisodeCounter += 1
        mPodcastDetails = podcastDetails
        if (!mCacheWriteError) {
            listener.onNextEpisodeFetchSuccess(EpisodeMocks.GET_NEXT_EPISODE())
        } else {
            listener.onNextEpisodeFetchFailed()
        }
    }

    var mGetPreviousEpisodeCounter = 0
    override fun getPreviousEpisode(
        episodeId: String,
        publishDate: Long,
        listener: PreviousEpisodeListener
    ) {
        mGetPreviousEpisodeCounter += 1
        mEpisodeId = episodeId
        mPublishDate = publishDate
        if (!mCacheQueryError) {
            if (!mEpisodeNotInCache) {
                listener.onPreviousEpisodeFetchSuccess(EpisodeMocks.GET_PREV_EPISODE())
            } else {
                listener.onPreviousEpisodeFetchSuccess(null)
            }
        } else {
            listener.onPreviousEpisodeFetchFailed()
        }
    }
}
