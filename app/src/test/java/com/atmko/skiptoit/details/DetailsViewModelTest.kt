package com.atmko.skiptoit.details

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.EpisodesCache
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
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeCalled() {
        // Arrange
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mEpisodesCacheTd.mRestoreEpisodeCounter, `is`(1))
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessNullEpisodeReturned_notifyNewPodcastDetected() {
        // Arrange
        noLastEpisode()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onNewPodcastDetcted()
        verify(mListenerMock2).onNewPodcastDetcted()
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessNewPodcastDected_notifyNewPodcastDected() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onNewPodcastDetcted()
        verify(mListenerMock2).onNewPodcastDetcted()
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessIsLastPlayedPodcast_notifyOldPodcastDected() {
        // Arrange
        isLastPlayedPodcast()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onOldPodcastDetced()
        verify(mListenerMock2).onOldPodcastDetced()
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessOldPodcastDected_unsubscribedListenerNotNotified() {
        // Arrange
        isLastPlayedPodcast()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onOldPodcastDetced()
        verify(mListenerMock2, never()).onOldPodcastDetced()
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeError_listenersNotifiedOfError() {
        // Arrange
        restoreEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onPodcastDetectFailed()
        verify(mListenerMock1).onPodcastDetectFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun togglePlaybackAndNotify_isOldPodcastNull_notifyToggleOldEpisodePlaybackFailed() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onToggleOldEpisodePlaybackFailed()
        verify(mListenerMock2).onToggleOldEpisodePlaybackFailed()
    }

    @Test
    fun togglePlaybackAndNotify_isOldPodcast_notifyToggleOldEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = true
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onToggleOldEpisodePlayback()
        verify(mListenerMock2).onToggleOldEpisodePlayback()
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcast_correctPodcastIdSentToGetAllPodcastEpisodes() {
        // Arrange
        SUT.isOldPodcast = false
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mEpisodesCacheTd.mGetAllPodcastEpisodesCounter, `is`(1))
        assertThat(mEpisodesCacheTd.mGetAllPodcastEpisodesArgPodcastId, `is`(PodcastMocks.PODCAST_ID_1)
        )
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessNoEpisodesAvailable_nullSavedToLatestEpisodeIdVariable() {
        // Arrange
        SUT.latestEpisodeId = null
        SUT.isOldPodcast = false
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertNull(SUT.latestEpisodeId)
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessNoEpisodesAvailable_notifyToggleNewEpisodePlaybackFailed() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlaybackFailed()
        verify(mListenerMock2).onToggleNewEpisodePlaybackFailed()
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_latestEpisodeIdSavedToVariable() {
        // Arrange
        SUT.isOldPodcast = false
        getAllEpisodesEpisodesAvailable()
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(SUT.latestEpisodeId, `is`(EpisodeMocks.EPISODE_ID_2))
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_notifyToggleNewEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = false
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlayback(EpisodeMocks.EPISODE_ID_2)
        verify(mListenerMock2).onToggleNewEpisodePlayback(EpisodeMocks.EPISODE_ID_2)
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailableSecondCall_getAllPodcastEpisodesCalledOnlyOnceNotifyToggleOldEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.latestEpisodeId = null
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mEpisodesCacheTd.mGetAllPodcastEpisodesCounter, `is`(1))
        verify(mListenerMock1).onToggleOldEpisodePlayback()
        verify(mListenerMock2).onToggleOldEpisodePlayback()
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_unregisteredListenersNotNotified() {
        // Arrange
        SUT.isOldPodcast = false
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlayback(EpisodeMocks.EPISODE_ID_2)
        verify(mListenerMock2, never()).onToggleNewEpisodePlayback(EpisodeMocks.EPISODE_ID_2)
    }

    @Test
    fun togglePlaybackAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_notifyToggleNewEpisodePlaybackFailed() {
        // Arrange
        SUT.isOldPodcast = false
        getAllPodcastEpisodesFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlaybackAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlaybackFailed()
        verify(mListenerMock2).onToggleNewEpisodePlaybackFailed()
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

    class EpisodesCacheTd : EpisodesCache(null, null) {

        var mGetAllPodcastEpisodesCounter = 0
        lateinit var mGetAllPodcastEpisodesArgPodcastId: String
        var mGetAllPodcastEpisodesEpisodesAvailable = false
        var mGetAllPodcastEpisodesFailure = false
        override fun getAllPodcastEpisodes(podcastId: String, listener: GetAllPodcastEpisodesListener) {
            mGetAllPodcastEpisodesCounter += 1
            mGetAllPodcastEpisodesArgPodcastId = podcastId
            if (mGetAllPodcastEpisodesFailure) {
                listener.onGetAllEpisodesFailed()
                return
            }
            if (!mGetAllPodcastEpisodesEpisodesAvailable) {
                listener.onGetAllEpisodesSuccess(listOf())
            } else {
                listener.onGetAllEpisodesSuccess(
                    listOf(
                        EpisodeMocks.GET_EPISODE_2(),
                        EpisodeMocks.GET_EPISODE_1()
                    )
                )
            }
        }

        var mRestoreEpisodeCounter = 0
        var mRestoreEpisodeFailure = false
        var mIsLastPlayedPodcast: Boolean? = false
        override fun restoreEpisode(listener: RestoreEpisodeListener) {
            mRestoreEpisodeCounter += 1
            if (!mRestoreEpisodeFailure) {
                val restoredEpisodeMock = EpisodeMocks.GET_EPISODE_DETAILS()
                if (mIsLastPlayedPodcast == null) {
                    listener.onEpisodeRestoreSuccess(null)
                    return
                }
                if (!mIsLastPlayedPodcast!!) {
                    restoredEpisodeMock.podcastId = PodcastMocks.PODCAST_ID_2
                    listener.onEpisodeRestoreSuccess(restoredEpisodeMock)
                } else {
                    restoredEpisodeMock.podcastId = PodcastMocks.PODCAST_ID_1
                    listener.onEpisodeRestoreSuccess(restoredEpisodeMock)
                }
            } else {
                listener.onEpisodeRestoreFailed()
            }
        }
    }
    // endregion helper classes
}


