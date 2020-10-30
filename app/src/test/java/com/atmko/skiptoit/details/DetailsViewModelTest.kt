package com.atmko.skiptoit.details

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.testclass.EpisodesCacheTd
import com.atmko.skiptoit.testdata.EpisodeMocks
import com.atmko.skiptoit.testdata.PodcastMocks
import com.atmko.skiptoit.testutils.TestUtils
import com.atmko.skiptoit.testutils.kotlinCapture
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DetailsViewModelTest {

    // region constants
    companion object {
        const val PODCAST_ID = "podcastId"
    }
    // endregion constants

    // end region helper fields
    lateinit var mPodcastDetailsEndpointTd: PodcastDetailsEndpointTd
    lateinit var mEpisodesCacheTd: EpisodesCacheTd

    @Mock lateinit var mListenerMock1: DetailsViewModel.Listener
    @Mock lateinit var mListenerMock2: DetailsViewModel.Listener

    @Captor lateinit var mArgCaptorPodcastDetails: ArgumentCaptor<PodcastDetails>
    // endregion helper fields

    lateinit var SUT: DetailsViewModel

    @Before
    fun setup() {
        mPodcastDetailsEndpointTd = PodcastDetailsEndpointTd()
        mEpisodesCacheTd = EpisodesCacheTd()
        SUT = DetailsViewModel(
            mPodcastDetailsEndpointTd,
            mEpisodesCacheTd
        )
        detailsEndpointSuccess()
        restoreEpisodeSuccess()
        isNotLastPlayedPodcast()
        getAllPodcastEpisodesSuccess()
    }

    @Test
    fun getDetailsAndNotify_notifyProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun getDetailsAndNotify_correctPodcastIdPassedToEndpoint() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mPodcastDetailsEndpointTd.mGetPodcastDetailsCounter, `is`(1))
        assertThat(mPodcastDetailsEndpointTd.mPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
    }

    @Test
    fun getCachedCommentAndNotify_podcastDetailsEndpointSuccess_commentSavedInViewModel() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mPodcastDetailsEndpointTd.mGetPodcastDetailsCounter, `is`(1))
        assertThat(SUT.podcastDetails, `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES()))
    }

    @Test
    fun getDetailsAndNotify_podcastDetailsEndpointSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onDetailsFetched(mArgCaptorPodcastDetails.kotlinCapture())
        verify(mListenerMock2).onDetailsFetched(mArgCaptorPodcastDetails.kotlinCapture())
        val captures: List<PodcastDetails> = mArgCaptorPodcastDetails.allValues
        assertThat(captures[0], `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES()))
        assertThat(captures[1], `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES()))
    }

    @Test
    fun getDetailsAndNotify_podcastDetailsEndpointSuccess_useCachedValueUponSecondCall() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mPodcastDetailsEndpointTd.mGetPodcastDetailsCounter, `is`(1))
        assertThat(SUT.podcastDetails, `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES()))
    }

    @Test
    fun getDetailsAndNotify_podcastDetailsEndpointSuccess_unregisteredListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onDetailsFetched(TestUtils.kotlinAny(PodcastDetails::class.java))
        verify(mListenerMock2, never()).onDetailsFetched(TestUtils.kotlinAny(PodcastDetails::class.java))
    }

    @Test
    fun getDetailsAndNotify_podcastDetailsEndpointError_listenersNotifiedOfError() {
        // Arrange
        detailsEndpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onDetailsFetchFailed()
        verify(mListenerMock2).onDetailsFetchFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun checkIsLastPlayedPodcastAndNotify_restoreEpisodeCalled() {
        // Arrange
        // Act
        SUT.checkIsLastPlayedPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mEpisodesCacheTd.mRestoreEpisodeCounter, `is`(1))
    }

    @Test
    fun checkIsLastPlayedPodcastAndNotify_restoreEpisodeSuccessNullEpisodeReturned_listenersNotifiedWithFalseValue() {
        // Arrange
        noLastEpisode()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkIsLastPlayedPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onIsLastPlayedPodcastFetched(false)
        verify(mListenerMock2).onIsLastPlayedPodcastFetched(false)
    }

    @Test
    fun checkIsLastPlayedPodcastAndNotify_restoreEpisodeSuccessIsNotLastPlayedPodcast_listenersNotifiedWithFalseValue() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkIsLastPlayedPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onIsLastPlayedPodcastFetched(false)
        verify(mListenerMock2).onIsLastPlayedPodcastFetched(false)
    }

    @Test
    fun checkIsLastPlayedPodcastAndNotify_restoreEpisodeSuccessIsLastPlayedPodcast_listenersNotifiedWithTrueValue() {
        // Arrange
        isLastPlayedPodcast()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkIsLastPlayedPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onIsLastPlayedPodcastFetched(true)
        verify(mListenerMock2).onIsLastPlayedPodcastFetched(true)
    }

    @Test
    fun checkIsLastPlayedPodcastAndNotify_restoreEpisodeSuccessIsLastPlayedPodcast_unsubscribedListenerNotNotified() {
        // Arrange
        isLastPlayedPodcast()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.checkIsLastPlayedPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onIsLastPlayedPodcastFetched(true)
        verify(mListenerMock2, never()).onIsLastPlayedPodcastFetched(true)
    }

    @Test
    fun checkIsCurrentlyPlayingPodcastAndNotify_restoreEpisodeError_listenersNotifiedOfError() {
        // Arrange
        restoreEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkIsLastPlayedPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onIsLastPlayedPodcastFetchFailed()
        verify(mListenerMock1).onIsLastPlayedPodcastFetchFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun getLatestEpisodeIdAndNotify_correctPodcastIdSentToGetAllPodcastEpisodes() {
        // Arrange
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mEpisodesCacheTd.mGetAllPodcastEpisodesCounter, `is`(1))
        assertThat(mEpisodesCacheTd.mGetAllPodcastEpisodesArgPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessNoEpisodesAvailable_nullSavedToLatestEpisodeIdVariable() {
        // Arrange
        SUT.latestEpisodeId = null
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertNull(SUT.latestEpisodeId)
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessNoEpisodesAvailable_listenersNotifiedWithNullValue() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onLatestEpisodeIdFetched(null)
        verify(mListenerMock2).onLatestEpisodeIdFetched(null)
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessEpisodesAvailable_latestEpisodeIdSavedToVariable() {
        // Arrange
        getAllEpisodesEpisodesAvailable()
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(SUT.latestEpisodeId, `is`(EpisodeMocks.EPISODE_ID_2))
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessEpisodesAvailable_listenersNotifiedWithCorrectValue() {
        // Arrange
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onLatestEpisodeIdFetched(EpisodeMocks.EPISODE_ID_2)
        verify(mListenerMock2).onLatestEpisodeIdFetched(EpisodeMocks.EPISODE_ID_2)
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessEpisodesAvailableSecondCall_getAllPodcastEpisodesCalledOnlyOnce() {
        // Arrange
        SUT.latestEpisodeId = null
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mEpisodesCacheTd.mGetAllPodcastEpisodesCounter, `is`(1))
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessEpisodesAvailable_unregisteredListenersNotNotified() {
        // Arrange
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onLatestEpisodeIdFetched(EpisodeMocks.EPISODE_ID_2)
        verify(mListenerMock2, never()).onLatestEpisodeIdFetched(EpisodeMocks.EPISODE_ID_2)
    }

    @Test
    fun getLatestEpisodeIdAndNotify_getAllPodcastEpisodesSuccessEpisodesAvailable_listenersNotifiedOfError() {
        // Arrange
        getAllPodcastEpisodesFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getLatestEpisodeIdAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onLatestEpisodeIdFetchFailed()
        verify(mListenerMock2).onLatestEpisodeIdFetchFailed()
    }

    // region helper methods
    fun detailsEndpointSuccess() {
        // no-op because mFailure false by default
    }

    fun detailsEndpointError() {
        mPodcastDetailsEndpointTd.mFailure = true
    }

    fun restoreEpisodeSuccess() {
        // no-op because mRestoreEpisodeFailure false by default
    }

    fun restoreEpisodeError() {
        mEpisodesCacheTd.mRestoreEpisodeFailure = true
    }

    fun isNotLastPlayedPodcast() {
        // no-op because mIsLastPlayedPodcast false by default
    }

    fun isLastPlayedPodcast() {
        mEpisodesCacheTd.mIsLastPlayedPodcast = true
    }

    fun noLastEpisode() {
        mEpisodesCacheTd.mIsLastPlayedPodcast = null
    }

    fun getAllEpisodesNoEpisodesAvailable() {
        // no-op because mGetAllPodcastEpisodesEpisodesAvailable false by default
    }

    fun getAllEpisodesEpisodesAvailable() {
        mEpisodesCacheTd.mGetAllPodcastEpisodesEpisodesAvailable = true
    }

    fun getAllPodcastEpisodesSuccess() {
        // no-op because mGetAllPodcastEpisodesFailure false by default
    }

    fun getAllPodcastEpisodesFailure() {
        mEpisodesCacheTd.mGetAllPodcastEpisodesFailure = true
    }
    // endregion helper methods

    // region helper classes
    class PodcastDetailsEndpointTd : PodcastDetailsEndpoint(null) {

        var mGetPodcastDetailsCounter = 0
        var mPodcastId = ""
        var mFailure = false
        override fun getPodcastDetails(podcastId: String, listener: Listener) {
            mGetPodcastDetailsCounter += 1
            mPodcastId = podcastId
            if (!mFailure) {
                listener.onPodcastDetailsFetchSuccess(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
            } else {
                listener.onPodcastDetailsFetchFailed()
            }
        }
    }
    // endregion helper classes
}


