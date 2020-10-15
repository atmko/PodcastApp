package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Episode
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

    var mDeletePodcastEpisodesCounter = 0
    var mDeletePodcastEpisodesError = false
    var mPodcastId = ""
    override fun deletePodcastEpisodes(currentPodcastId: String, listener: DeletePodcastEpisodesListener) {
        mDeletePodcastEpisodesCounter += 1
        mPodcastId = currentPodcastId
        if (!mDeletePodcastEpisodesError) {
            listener.onDeletePodcastEpisodesSuccess()
        } else {
            listener.onDeletePodcastEpisodesFailed()
        }
    }

    var mSaveEpisodeCounter = 0
    var mSaveEpisodeError = false
    lateinit var mSaveEpisodeArgEpisode: Episode
    override fun saveEpisode(episode: Episode, listener: SaveEpisodeListener) {
        mSaveEpisodeCounter += 1
        mSaveEpisodeArgEpisode = episode
        if (!mSaveEpisodeError) {
            listener.onEpisodeSaveSuccess()
        } else {
            listener.onEpisodeSaveFailed()
        }
    }

    var mRestoreEpisodeCounter = 0
    var mRestoreEpisodeFailure = false
    override fun restoreEpisode(listener: RestoreEpisodeListener) {
        mRestoreEpisodeCounter += 1
        if (!mRestoreEpisodeFailure) {
            listener.onEpisodeRestoreSuccess(EpisodeMocks.GET_EPISODE_DETAILS())
        } else {
            listener.onEpisodeRestoreFailed()
        }
    }

    var mClearLastPlayedEpisodeCounter = 0
    var mClearLastPlayedEpisodeError = false
    override fun clearLastPlayedEpisode(listener: ClearLastPlayedEpisodeListener) {
        mClearLastPlayedEpisodeCounter += 1
        if (!mClearLastPlayedEpisodeError) {
            listener.onEpisodeClearSuccess()
        } else {
            listener.onEpisodeClearFailed()
        }
    }

    var mGetNextEpisodeCounter = 0
    var mGetNextEpisodeError = false
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
        if (!mGetNextEpisodeError) {
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
    var mInsertEpisodesAndReturnNextEpisodeError = false
    override fun insertEpisodesAndReturnNextEpisode(
        podcastDetails: PodcastDetails,
        listener: NextEpisodeListener
    ) {
        mInsertEpisodesAndReturnNextEpisodeCounter += 1
        mPodcastDetails = podcastDetails
        if (!mInsertEpisodesAndReturnNextEpisodeError) {
            listener.onNextEpisodeFetchSuccess(EpisodeMocks.GET_NEXT_EPISODE())
        } else {
            listener.onNextEpisodeFetchFailed()
        }
    }

    var mGetPreviousEpisodeCounter = 0
    var mGetPreviousEpisodeError = false
    override fun getPreviousEpisode(
        episodeId: String,
        publishDate: Long,
        listener: PreviousEpisodeListener
    ) {
        mGetPreviousEpisodeCounter += 1
        mEpisodeId = episodeId
        mPublishDate = publishDate
        if (!mGetPreviousEpisodeError) {
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
