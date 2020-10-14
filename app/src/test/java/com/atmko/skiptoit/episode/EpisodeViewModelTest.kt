package com.atmko.skiptoit.episode

import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.testclass.EpisodesCacheTd
import com.atmko.skiptoit.testdata.EpisodeMocks
import com.atmko.skiptoit.testdata.PodcastMocks
import com.atmko.skiptoit.testutils.TestUtils
import com.atmko.skiptoit.testutils.kotlinCapture
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EpisodeViewModelTest {

    // region constants
    companion object {
        const val PODCAST_ID = "podcastId"
        const val EPISODE_ID = "episodeId"
    }
    // endregion constants

    // end region helper fields
    private lateinit var mEpisodesCacheTd: EpisodesCacheTd
    private lateinit var mEpisodeEndpointTd: EpisodeEndpointTd

    @Mock
    lateinit var mListenerMock1: EpisodeViewModel.Listener
    @Mock
    lateinit var mListenerMock2: EpisodeViewModel.Listener
    // endregion helper fields

    lateinit var SUT: EpisodeViewModel

    @Before
    fun setup() {
        mEpisodesCacheTd = EpisodesCacheTd()
        mEpisodeEndpointTd = EpisodeEndpointTd()
        SUT = EpisodeViewModel(mEpisodeEndpointTd, mEpisodesCacheTd)

        cacheSuccess()
        endpointSuccess()
        //----------------
        cacheQuerySuccess()
        episodeInCache()
        nextEpisodesAvailable()
        cacheWriteSuccess()
    }

    @Test
    fun clearPodcastCacheAndNotify_correctPodcastIdPassedToCache() {
        // Arrange
        // Act
        SUT.clearPodcastCacheAndNotify(PODCAST_ID)
        // Assert
        assertThat(mEpisodesCacheTd.mPodcastId, `is`(PODCAST_ID))
    }

    @Test
    fun clearPodcastCacheAndNotify_cacheSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.clearPodcastCacheAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onPodcastEpisodesCacheCleared()
        verify(mListenerMock2).onPodcastEpisodesCacheCleared()
    }

    @Test
    fun clearPodcastCacheAndNotify_cacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.clearPodcastCacheAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onPodcastEpisodesCacheCleared()
        verify(mListenerMock2, never()).onPodcastEpisodesCacheCleared()
    }

    @Test
    fun clearPodcastCacheAndNotify_cacheError_listenersNotifiedOfError() {
        // Arrange
        cacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.clearPodcastCacheAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onPodcastEpisodesCacheClearFailed()
        verify(mListenerMock2).onPodcastEpisodesCacheClearFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun getDetailsAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun getDetailsAndNotify_correctEpisodeIdPassedToEndpoint() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        assertThat(mEpisodeEndpointTd.mPodcastId, `is`(EPISODE_ID))
    }

    @Test
    fun getDetailsAndNotify_endpointSuccess_episodeDetailsSavedToVariable() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        assertThat(mEpisodeEndpointTd.mGetEpisodeDetailsCounter, `is`(1))
        assertThat(SUT.episodeDetails, `is`(EpisodeMocks.GET_EPISODE()))
    }

    @Test
    fun getDetailsAndNotify_endpointSuccess_listenersNotifiedOfSuccessWithCorrectEpisode() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Episode> = ArgumentCaptor.forClass(Episode::class.java)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetched(ac.kotlinCapture())
        verify(mListenerMock2).onDetailsFetched(ac.kotlinCapture())
        val captures: List<Episode> = ac.allValues
        assertThat(captures[0], `is`(EpisodeMocks.GET_EPISODE()))
        assertThat(captures[1], `is`(EpisodeMocks.GET_EPISODE()))
    }

    @Test
    fun getDetailsAndNotify_endpointSuccess_secondCallUsesSavedVariable() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        assertThat(mEpisodeEndpointTd.mGetEpisodeDetailsCounter, `is`(1))
        assertThat(SUT.episodeDetails, `is`(EpisodeMocks.GET_EPISODE()))
    }

    @Test
    fun getDetailsAndNotify_endpointSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock2, never()).onDetailsFetched(TestUtils.kotlinAny(Episode::class.java))
    }

    @Test
    fun getDetailsAndNotify_endpointError_listenersNotifiedOfError() {
        // Arrange
        endpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetchFailed()
        verify(mListenerMock2).onDetailsFetchFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun restoreEpisodeAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.restoreEpisodeAndNotify()
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun restoreEpisodeAndNotify_cacheSuccess_episodeDetailsSavedToVariable() {
        // Arrange
        // Act
        SUT.restoreEpisodeAndNotify()
        // Assert
        assertThat(mEpisodesCacheTd.mRestoreEpisodeCounter, `is`(1))
        assertThat(SUT.episodeDetails, `is`(EpisodeMocks.GET_EPISODE()))
    }

    @Test
    fun restoreEpisodeAndNotify_cacheSuccess_listenersNotifiedOfSuccessWithCorrectEpisode() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Episode> = ArgumentCaptor.forClass(Episode::class.java)
        // Act
        SUT.restoreEpisodeAndNotify()
        // Assert
        verify(mListenerMock1).onDetailsFetched(ac.kotlinCapture())
        verify(mListenerMock2).onDetailsFetched(ac.kotlinCapture())
        val captures: List<Episode> = ac.allValues
        assertThat(captures[0], `is`(EpisodeMocks.GET_EPISODE()))
        assertThat(captures[1], `is`(EpisodeMocks.GET_EPISODE()))
    }

    @Test
    fun restoreEpisodeAndNotify_cacheSuccess_secondCallUsesSavedVariable() {
        // Arrange
        // Act
        SUT.restoreEpisodeAndNotify()
        SUT.restoreEpisodeAndNotify()
        // Assert
        assertThat(mEpisodesCacheTd.mRestoreEpisodeCounter, `is`(1))
        assertThat(SUT.episodeDetails, `is`(EpisodeMocks.GET_EPISODE()))
    }

    @Test
    fun restoreEpisodeAndNotify_cacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.restoreEpisodeAndNotify()
        // Assert
        verify(mListenerMock1).onDetailsFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock2, never()).onDetailsFetched(TestUtils.kotlinAny(Episode::class.java))
    }

    @Test
    fun restoreEpisodeAndNotify_cacheError_listenersNotifiedOfError() {
        // Arrange
        cacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.restoreEpisodeAndNotify()
        // Assert
        verify(mListenerMock1).onDetailsFetchFailed()
        verify(mListenerMock2).onDetailsFetchFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun fetchNextEpisodeAndNotify_correctEpisodeIdAndPublishDatePassedToCache() {
        // Arrange
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mEpisodeId, `is`(EpisodeMocks.GET_EPISODE().episodeId))
        assertThat(mEpisodesCacheTd.mPublishDate, `is`(EpisodeMocks.GET_EPISODE().publishDate))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccess_cacheInvoked() {
        // Arrange
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mGetNextEpisodeCounter, `is`(1))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQueryError_cacheInvokedListenersNotifiedOfError() {
        // Arrange
        cacheQueryError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mGetNextEpisodeCounter, `is`(1))
        verify(mListenerMock1).onNextEpisodeFetchFailed()
        verify(mListenerMock2).onNextEpisodeFetchFailed()
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_nextEpisodeSavedToVariable() {
        // Arrange
        SUT.nextEpisode = null
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(SUT.nextEpisode, `is`(EpisodeMocks.GET_NEXT_EPISODE()))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_listenersNotifiedOfSuccessWithCorrectEpisode() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Episode> = ArgumentCaptor.forClass(Episode::class.java)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        verify(mListenerMock1).onNextEpisodeFetched(ac.kotlinCapture())
        verify(mListenerMock2).onNextEpisodeFetched(ac.kotlinCapture())
        val captures = ac.allValues
        assertThat(captures[0], `is`(EpisodeMocks.GET_NEXT_EPISODE()))
        assertThat(captures[1], `is`(EpisodeMocks.GET_NEXT_EPISODE()))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_secondCallUsesSavedVariableAndDoesNotAccessCache() {
        // Arrange
        SUT.nextEpisode = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mGetNextEpisodeCounter, `is`(1))
        assertThat(SUT.nextEpisode, `is`(EpisodeMocks.GET_NEXT_EPISODE()))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        verify(mListenerMock1).onNextEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock2, never()).onNextEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCache_correctPodcastIdAndPublishDatePassedToEndpoint() {
        // Arrange
        episodeNotInCache()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodeEndpointTd.mPodcastId, `is`(PODCAST_ID))
        assertThat(mEpisodeEndpointTd.mEpisodePublishDate, `is`(EpisodeMocks.GET_EPISODE().publishDate))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccess_endpointInvoked() {
        // Arrange
        episodeNotInCache()
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodeEndpointTd.mFetchNextEpisodesCounter, `is`(1))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQueryError_endpointInvokedListenersNotifiedOfError() {
        // Arrange
        episodeNotInCache()
        endpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodeEndpointTd.mFetchNextEpisodesCounter, `is`(1))
        verify(mListenerMock1).onNextEpisodeFetchFailed()
        verify(mListenerMock2).onNextEpisodeFetchFailed()
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailable_correctPodcastDetailsPassedToCache() {
        // Arrange
        episodeNotInCache()
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnNextEpisodeCounter, `is`(1))
        assertThat(mEpisodesCacheTd.mPodcastDetails, `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITH_EPISODES()))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesUnavailable_saveEpisodesNotCalledListenersNotNotified() {
        // Arrange
        episodeNotInCache()
        nextEpisodesUnavailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnNextEpisodeCounter, `is`(0))
        verify(mListenerMock1, never()).onNextEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock2, never()).onNextEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock1, never()).onNextEpisodeFetchFailed()
        verify(mListenerMock2, never()).onNextEpisodeFetchFailed()
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailableSaveEpisodesSuccess_nextEpisodeSavedToVariable() {
        // Arrange
        SUT.nextEpisode = null
        episodeNotInCache()
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnNextEpisodeCounter, `is`(1))
        assertThat(SUT.nextEpisode, `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITH_EPISODES().episodes[0]))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailableSaveEpisodesSuccess_listenersNotifiedWithCorrectEpisode() {
        // Arrange
        episodeNotInCache()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Episode> = ArgumentCaptor.forClass(Episode::class.java)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnNextEpisodeCounter, `is`(1))
        verify(mListenerMock1).onNextEpisodeFetched(ac.kotlinCapture())
        verify(mListenerMock2).onNextEpisodeFetched(ac.kotlinCapture())
        val captures = ac.allValues
        assertThat(captures[0], `is`(EpisodeMocks.GET_NEXT_EPISODE()))
        assertThat(captures[1], `is`(EpisodeMocks.GET_NEXT_EPISODE()))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailableSaveEpisodesError_listenersNotifiedOfError() {
        // Arrange
        episodeNotInCache()
        cacheWriteError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(PODCAST_ID, EpisodeMocks.GET_EPISODE())
        // Assert
        verify(mListenerMock1).onNextEpisodeFetchFailed()
        verify(mListenerMock2).onNextEpisodeFetchFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun fetchPrevEpisodeAndNotify_correctEpisodeIdAndPublishDatePassedToCache() {
        // Arrange
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mEpisodeId, `is`(EpisodeMocks.GET_EPISODE().episodeId))
        assertThat(mEpisodesCacheTd.mPublishDate, `is`(EpisodeMocks.GET_EPISODE().publishDate))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccess_cacheInvoked() {
        // Arrange
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mGetPreviousEpisodeCounter, `is`(1))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQueryError_cacheInvokedListenersNotifiedOfError() {
        // Arrange
        cacheQueryError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mGetPreviousEpisodeCounter, `is`(1))
        verify(mListenerMock1).onPreviousEpisodeFetchFailed()
        verify(mListenerMock2).onPreviousEpisodeFetchFailed()
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_prevEpisodeSavedToVariable() {
        // Arrange
        SUT.prevEpisode = null
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(SUT.prevEpisode, `is`(EpisodeMocks.GET_PREV_EPISODE()))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_listenersNotifiedOfSuccessWithCorrectEpisode() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Episode> = ArgumentCaptor.forClass(Episode::class.java)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        verify(mListenerMock1).onPreviousEpisodeFetched(ac.kotlinCapture())
        verify(mListenerMock2).onPreviousEpisodeFetched(ac.kotlinCapture())
        val captures = ac.allValues
        assertThat(captures[0], `is`(EpisodeMocks.GET_PREV_EPISODE()))
        assertThat(captures[1], `is`(EpisodeMocks.GET_PREV_EPISODE()))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_secondCallUsesSavedVariableAndDoesNotAccessCache() {
        // Arrange
        SUT.prevEpisode = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        assertThat(mEpisodesCacheTd.mGetPreviousEpisodeCounter, `is`(1))
        assertThat(SUT.prevEpisode, `is`(EpisodeMocks.GET_PREV_EPISODE()))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeInCache_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE())
        // Assert
        verify(mListenerMock1).onPreviousEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock2, never()).onPreviousEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
    }

    // region helper methods
    private fun endpointSuccess() {
        // no-op because mFailure false by default
    }

    private fun endpointError() {
        mEpisodeEndpointTd.mFailure = true
    }

    private fun cacheSuccess() {
        // no-op because mFailure false by default
    }

    private fun cacheError() {
        mEpisodesCacheTd.mFailure = true
    }

    private fun cacheQuerySuccess() {
        // no-op because mCacheQueryFailure false by default
    }

    private fun cacheQueryError() {
        mEpisodesCacheTd.mCacheQueryError = true
    }

    private fun episodeInCache() {
        // no-op because mEpisodeNotInCache false by default
    }

    private fun episodeNotInCache() {
        mEpisodesCacheTd.mEpisodeNotInCache = true
    }

    private fun nextEpisodesAvailable() {
        // no-op because mNoNextEpisodes false by default
    }

    private fun nextEpisodesUnavailable() {
        mEpisodeEndpointTd.mNoNextEpisodes = true
    }

    private fun cacheWriteSuccess() {
        // no-op because mCacheWriteError false by default
    }

    private fun cacheWriteError() {
        mEpisodesCacheTd.mCacheWriteError = true
    }
    // endregion helper methods

    // region helper classes
    class EpisodeEndpointTd : EpisodeEndpoint(null) {

        var mGetEpisodeDetailsCounter = 0
        var mFailure = false
        var mPodcastId = ""
        override fun getEpisodeDetails(episodeId: String, listener: EpisodeDetailsListener) {
            mGetEpisodeDetailsCounter += 1
            mPodcastId = episodeId
            if (!mFailure) {
                listener.onEpisodeDetailsFetchSuccess(EpisodeMocks.GET_EPISODE())
            } else {
                listener.onEpisodeDetailsFetchFailed()
            }
        }

        var mFetchNextEpisodesCounter = 0
        var mNoNextEpisodes = false
        var mEpisodePublishDate: Long? = null
        override fun fetchNextEpisodes(podcastId: String, episodePublishDate: Long, listener: BatchNextEpisodeListener) {
            mFetchNextEpisodesCounter += 1
            mPodcastId = podcastId
            mEpisodePublishDate = episodePublishDate
            if (!mFailure) {
                if (!mNoNextEpisodes) {
                    listener.onBatchNextEpisodesFetchSuccess(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITH_EPISODES())
                } else {
                    listener.onBatchNextEpisodesFetchSuccess(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
                }
            } else {
                listener.onBatchNextEpisodesFetchFailed()
            }
        }
    }
    // endregion helper classes
}