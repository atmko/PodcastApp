package com.atmko.skiptoit.episode

import com.atmko.skiptoit.episodelist.EpisodeBoundaryCallbackTest
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
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
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
    private lateinit var mGetEpisodesEndpointTd: EpisodeBoundaryCallbackTest.GetEpisodesEndpointTd

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
        mGetEpisodesEndpointTd = EpisodeBoundaryCallbackTest.GetEpisodesEndpointTd()
        SUT = EpisodeViewModel(mEpisodeEndpointTd, mGetEpisodesEndpointTd, mEpisodesCacheTd)

        //----------------
        getEpisodesSuccess()
        getNextEpisodeSuccess()
        getPreviousEpisodesSuccess()
        episodeInCache()
        prevEpisodesAvailable()
        insertEpisodesAndReturnPrevEpisodeSuccess()
        deletePodcastEpisodesSuccess()
        getEpisodeDetailsSuccess()
        saveEpisodeSuccess()
        restoreEpisodeSuccess()
    }

    @Test
    fun getDetailsAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastId_correctPodcastIdPassedToClearEpisodes() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        assertThat(mEpisodesCacheTd.mDeletePodcastEpisodesCounter, `is`(1))
        assertThat(mEpisodesCacheTd.mPodcastId, `is`(PODCAST_ID))
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccess_correctEpisodeIdPassedToGetEpisodeDetails() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        assertThat(mEpisodeEndpointTd.mGetEpisodeDetailsCounter, `is`(1))
        assertThat(mEpisodeEndpointTd.mEpisodeId, `is`(EPISODE_ID))
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccessGetEpisodeDetailsSuccess_correctEpisodePassedToSaveEpisode() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        assertThat(mEpisodesCacheTd.mSaveEpisodeCounter, `is`(1))
        assertThat(
            mEpisodesCacheTd.mSaveEpisodeArgEpisode,
            `is`(EpisodeMocks.GET_EPISODE_DETAILS())
        )
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccessGetEpisodeDetailsSuccessSaveEpisodeSuccess_episodeDetailsSavedToVariable() {
        // Arrange
        SUT.episodeDetails = null
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        assertThat(SUT.episodeDetails, `is`(EpisodeMocks.GET_EPISODE_DETAILS()))
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccessGetEpisodeDetailsSuccessSaveEpisodeSuccess_listenersNotifiedOfSuccessWithCorrectValues() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetched(SUT.episodeDetails, false)
        verify(mListenerMock2).onDetailsFetched(SUT.episodeDetails, false)
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccessGetEpisodeDetailsSuccessSaveEpisodeSuccess_unregisteredListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetched(SUT.episodeDetails, false)
        verify(mListenerMock2, never()).onDetailsFetched(SUT.episodeDetails, false)
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccessGetEpisodeDetailsSuccessSaveEpisodeError_listenersNotifiedOfError() {
        // Arrange
        saveEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetchFailed()
        verify(mListenerMock2).onDetailsFetchFailed()
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesSuccessGetEpisodeDetailsError_listenersNotifiedOfError() {
        // Arrange
        getEpisodeDetailsError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetchFailed()
        verify(mListenerMock2).onDetailsFetchFailed()
    }

    @Test
    fun getDetailsAndNotify_validEpisodeIdAndPodcastIdDeletePodcastEpisodesError_listenersNotifiedOfError() {
        // Arrange
        deletePodcastEpisodesError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(EPISODE_ID, PODCAST_ID)
        // Assert
        verify(mListenerMock1).onDetailsFetchFailed()
        verify(mListenerMock2).onDetailsFetchFailed()
    }

    @Test
    fun getDetailsAndNotify_invalidEpisodeIdAndPodcastId_restoreEpisodesPolled() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(null, null)
        // Assert
        assertThat(mEpisodesCacheTd.mRestoreEpisodeCounter, `is`(1))
    }

    @Test
    fun getDetailsAndNotify_invalidEpisodeIdAndPodcastIdRestoreEpisodeSuccess_episodeDetailsSavedToVariable() {
        // Arrange
        SUT.episodeDetails = null
        // Act
        SUT.getDetailsAndNotify(null, null)
        // Assert
        assertThat(SUT.episodeDetails, `is`(EpisodeMocks.GET_EPISODE_DETAILS()))
    }

    @Test
    fun getDetailsAndNotify_invalidEpisodeIdAndPodcastIdRestoreEpisodeSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(null, null)
        // Assert
        verify(mListenerMock1).onDetailsFetched(SUT.episodeDetails, true)
        verify(mListenerMock1).onDetailsFetched(SUT.episodeDetails, true)
    }

    @Test
    fun getDetailsAndNotify_invalidEpisodeIdAndPodcastIdRestoreEpisodeSuccess_unregisteredListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(null, null)
        // Assert
        verify(mListenerMock1).onDetailsFetched(SUT.episodeDetails, true)
        verify(mListenerMock2, never()).onDetailsFetched(SUT.episodeDetails, true)
    }

    @Test
    fun getDetailsAndNotify_invalidEpisodeIdAndPodcastIdRestoreEpisodeError_listenersNotifiedOfError() {
        // Arrange
        restoreEpisodeError()
        // Act
        SUT.getDetailsAndNotify(null, null)
        // Assert
        assertThat(mEpisodesCacheTd.mRestoreEpisodeCounter, `is`(1))
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun fetchNextEpisodeAndNotify_correctEpisodeIdAndPublishDatePassedToCache() {
        // Arrange
        // Act
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(
            mEpisodesCacheTd.mGetNextEpisodeArgPodcastId,
            `is`(EpisodeMocks.GET_EPISODE_DETAILS().podcastId)
        )
        assertThat(
            mEpisodesCacheTd.mGetNextEpisodeArgEpisodeId,
            `is`(EpisodeMocks.GET_EPISODE_DETAILS().episodeId)
        )
        assertThat(mEpisodesCacheTd.mPublishDate, `is`(EpisodeMocks.GET_EPISODE_1().publishDate))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQuerySuccess_cacheInvoked() {
        // Arrange
        // Act
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mEpisodesCacheTd.mGetNextEpisodeCounter, `is`(1))
    }

    @Test
    fun fetchNextEpisodeAndNotify_cacheQueryError_cacheInvokedListenersNotifiedOfError() {
        // Arrange
        getNextEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchNextEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        verify(mListenerMock1).onNextEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(
            mListenerMock2,
            never()
        ).onNextEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun fetchPrevEpisodeAndNotify_correctEpisodeIdAndPublishDatePassedToCache() {
        // Arrange
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(
            mEpisodesCacheTd.mGetPreviousEpisodeArgPodcastId,
            `is`(EpisodeMocks.GET_EPISODE_DETAILS().podcastId)
        )
        assertThat(
            mEpisodesCacheTd.mGetPreviousEpisodeArgEpisodeId,
            `is`(EpisodeMocks.GET_EPISODE_DETAILS().episodeId)
        )
        assertThat(mEpisodesCacheTd.mPublishDate, `is`(EpisodeMocks.GET_EPISODE_1().publishDate))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccess_cacheInvoked() {
        // Arrange
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mEpisodesCacheTd.mGetPreviousEpisodeCounter, `is`(1))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQueryError_cacheInvokedListenersNotifiedOfError() {
        // Arrange
        getPreviousEpisodesError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
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
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        verify(mListenerMock1).onPreviousEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(
            mListenerMock2,
            never()
        ).onPreviousEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCache_correctPodcastIdAndPublishDatePassedToEndpoint() {
        // Arrange
        episodeNotInCache()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mGetEpisodesEndpointTd.mGetEpisodesArgPodcastId, `is`(PODCAST_ID))
        assertThat(
            mGetEpisodesEndpointTd.mGetEpisodesArgPublishedAfterDate,
            `is`(EpisodeMocks.GET_EPISODE_1().publishDate)
        )
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccess_endpointInvoked() {
        // Arrange
        episodeNotInCache()
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mGetEpisodesEndpointTd.mGetEpisodesCounter, `is`(1))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQueryError_endpointInvokedListenersNotifiedOfError() {
        // Arrange
        episodeNotInCache()
        getEpisodesError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mGetEpisodesEndpointTd.mGetEpisodesCounter, `is`(1))
        verify(mListenerMock1).onPreviousEpisodeFetchFailed()
        verify(mListenerMock2).onPreviousEpisodeFetchFailed()
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailable_correctPodcastDetailsPassedToCache() {
        // Arrange
        episodeNotInCache()
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnPrevEpisodeCounter, `is`(1))
        assertThat(
            mEpisodesCacheTd.mPodcastDetails,
            `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITH_EPISODES())
        )
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesUnavailable_saveEpisodesNotCalledListenersNotNotified() {
        // Arrange
        episodeNotInCache()
        prevEpisodesUnavailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnPrevEpisodeCounter, `is`(0))
        verify(
            mListenerMock1,
            never()
        ).onPreviousEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(
            mListenerMock2,
            never()
        ).onPreviousEpisodeFetched(TestUtils.kotlinAny(Episode::class.java))
        verify(mListenerMock1, never()).onPreviousEpisodeFetchFailed()
        verify(mListenerMock2, never()).onPreviousEpisodeFetchFailed()
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailableSaveEpisodesSuccess_prevEpisodeSavedToVariable() {
        // Arrange
        SUT.prevEpisode = null
        episodeNotInCache()
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnPrevEpisodeCounter, `is`(1))
        assertThat(
            SUT.prevEpisode,
            `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITH_EPISODES().episodes[0])
        )
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailableSaveEpisodesSuccess_listenersNotifiedWithCorrectEpisode() {
        // Arrange
        episodeNotInCache()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Episode> = ArgumentCaptor.forClass(Episode::class.java)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        assertThat(mEpisodesCacheTd.mInsertEpisodesAndReturnPrevEpisodeCounter, `is`(1))
        verify(mListenerMock1).onPreviousEpisodeFetched(ac.kotlinCapture())
        verify(mListenerMock2).onPreviousEpisodeFetched(ac.kotlinCapture())
        val captures = ac.allValues
        assertThat(captures[0], `is`(EpisodeMocks.GET_PREV_EPISODE()))
        assertThat(captures[1], `is`(EpisodeMocks.GET_PREV_EPISODE()))
    }

    @Test
    fun fetchPrevEpisodeAndNotify_cacheQuerySuccessEpisodeNotInCacheEndpointQuerySuccessEpisodesAvailableSaveEpisodesError_listenersNotifiedOfError() {
        // Arrange
        episodeNotInCache()
        insertEpisodesAndReturnPreviousEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.fetchPrevEpisodeAndNotify(EpisodeMocks.GET_EPISODE_DETAILS())
        // Assert
        verify(mListenerMock1).onPreviousEpisodeFetchFailed()
        verify(mListenerMock2).onPreviousEpisodeFetchFailed()
    }

    // region helper methods
    private fun getEpisodesSuccess() {
        // no-op because mGetEpisodesError false by default
    }

    private fun getEpisodesError() {
        mGetEpisodesEndpointTd.mGetEpisodesError = true
    }

    private fun getNextEpisodeSuccess() {
        // no-op because mGetNextEpisodeError false by default
    }

    private fun getNextEpisodeError() {
        mEpisodesCacheTd.mGetNextEpisodeError = true
    }

    private fun getPreviousEpisodesSuccess() {
        // no-op because mGetPreviousEpisodeError false by default
    }

    private fun getPreviousEpisodesError() {
        mEpisodesCacheTd.mGetPreviousEpisodeError = true
    }

    private fun episodeInCache() {
        // no-op because mEpisodeNotInCache false by default
    }

    private fun episodeNotInCache() {
        mEpisodesCacheTd.mEpisodeNotInCache = true
    }

    private fun prevEpisodesAvailable() {
        // no-op because mNoPreviousEpisodes false by default
    }

    private fun prevEpisodesUnavailable() {
        mGetEpisodesEndpointTd.mNoPreviousEpisodes = true
    }

    private fun insertEpisodesAndReturnPrevEpisodeSuccess() {
        // no-op because mInsertEpisodesAndReturnPrevEpisodeError false by default
    }

    private fun insertEpisodesAndReturnPreviousEpisodeError() {
        mEpisodesCacheTd.mInsertEpisodesAndReturnPrevEpisodeError = true
    }

    private fun deletePodcastEpisodesSuccess() {
        // no-op because mDeletePodcastEpisodesError false by default
    }

    private fun deletePodcastEpisodesError() {
        mEpisodesCacheTd.mDeletePodcastEpisodesError = true
    }

    private fun getEpisodeDetailsSuccess() {
        // no-op because mGetEpisodeDetailsError false by default
    }

    private fun getEpisodeDetailsError() {
        mEpisodeEndpointTd.mGetEpisodeDetailsError = true
    }

    private fun saveEpisodeSuccess() {
        // no-op because mSaveEpisodeError false by default
    }

    private fun saveEpisodeError() {
        mEpisodesCacheTd.mSaveEpisodeError = true
    }

    private fun restoreEpisodeSuccess() {
        // no-op because mRestoreEpisodeFailure false by default
    }

    private fun restoreEpisodeError() {
        mEpisodesCacheTd.mRestoreEpisodeFailure = true
    }
    // endregion helper methods

    // region helper classes
    class EpisodeEndpointTd : EpisodeEndpoint(null) {

        var mGetEpisodeDetailsCounter = 0
        var mGetEpisodeDetailsError = false
        lateinit var mEpisodeId: String
        override fun getEpisodeDetails(episodeId: String, listener: EpisodeDetailsListener) {
            mGetEpisodeDetailsCounter += 1
            mEpisodeId = episodeId
            if (!mGetEpisodeDetailsError) {
                listener.onEpisodeDetailsFetchSuccess(EpisodeMocks.GET_EPISODE_DETAILS())
            } else {
                listener.onEpisodeDetailsFetchFailed()
            }
        }
    }
    // endregion helper classes
}