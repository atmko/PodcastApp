package com.atmko.skiptoit.details

import com.atmko.skiptoit.model.Episode
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
        const val OLD_PODCAST_ID = "oldPodcastId"
        const val NEW_PODCAST_ID = "newPodcastId"
        const val OLD_EPISODE_ID = "oldEpisodeId"
        const val NEW_EPISODE_ID = "newEpisodeId"

        fun GET_OLD_EPISODE(): Episode {
            return Episode(
                OLD_EPISODE_ID,
                EpisodeMocks.TITLE,
                EpisodeMocks.DESCRIPTION,
                EpisodeMocks.IMAGE,
                EpisodeMocks.AUDIO,
                EpisodeMocks.PUBLISH_DATE_1,
                EpisodeMocks.LENTH_IN_SECONDS
            )
        }

        fun GET_NEW_EPISODE(): Episode {
            return Episode(
                NEW_EPISODE_ID,
                EpisodeMocks.TITLE,
                EpisodeMocks.DESCRIPTION,
                EpisodeMocks.IMAGE,
                EpisodeMocks.AUDIO,
                EpisodeMocks.PUBLISH_DATE_1,
                EpisodeMocks.LENTH_IN_SECONDS
            )
        }
    }
    // endregion constants

    // end region helper fields
    lateinit var mPodcastDetailsEndpointTd: PodcastDetailsEndpointTd
    lateinit var mEpisodesCacheForDetailsTd: EpisodesCacheTdForDetailsTd

    @Mock
    lateinit var mListenerMock1: DetailsViewModel.Listener
    @Mock
    lateinit var mListenerMock2: DetailsViewModel.Listener

    @Captor
    lateinit var mArgCaptorPodcastDetails: ArgumentCaptor<PodcastDetails>
    // endregion helper fields

    lateinit var SUT: DetailsViewModel

    @Before
    fun setup() {
        mPodcastDetailsEndpointTd = PodcastDetailsEndpointTd()
        mEpisodesCacheForDetailsTd = EpisodesCacheTdForDetailsTd()
        SUT = DetailsViewModel(
            mPodcastDetailsEndpointTd,
            mEpisodesCacheForDetailsTd
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
        assertThat(
            SUT.podcastDetails,
            `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
        )
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
        assertThat(
            captures[0],
            `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
        )
        assertThat(
            captures[1],
            `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
        )
    }

    @Test
    fun getDetailsAndNotify_podcastDetailsEndpointSuccess_useCachedValueUponSecondCall() {
        // Arrange
        // Act
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        SUT.getDetailsAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mPodcastDetailsEndpointTd.mGetPodcastDetailsCounter, `is`(1))
        assertThat(
            SUT.podcastDetails,
            `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
        )
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
        verify(
            mListenerMock2,
            never()
        ).onDetailsFetched(TestUtils.kotlinAny(PodcastDetails::class.java))
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
        SUT.detectOldOrNewPodcastAndNotify(PODCAST_ID)
        // Assert
        assertThat(mEpisodesCacheForDetailsTd.mRestoreEpisodeCounter, `is`(1))
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessNullEpisodeReturned_isNewPodcast() {
        // Arrange
        noLastEpisode()
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(SUT.isOldPodcast, `is`(false))
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessNewPodcastDected_isNewPodcast() {
        // Arrange
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(SUT.isOldPodcast, `is`(false))
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessIsNewPodcast_notifyNewPodcastDetected() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onNewPodcastDetected()
        verify(mListenerMock2).onNewPodcastDetected()
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessIsOldPodcast_isOldPodcast() {
        // Arrange
        isOldPodcast()
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(SUT.isOldPodcast, `is`(true))
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessIsOldPodcast_currentEpisodeIdUpdated() {
        // Arrange
        isOldPodcast()
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(SUT.currentEpisodeId, `is`(OLD_EPISODE_ID))
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessIsOldPodcast_notifyOldPodcastDected() {
        // Arrange
        isOldPodcast()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onOldPodcastDetected(OLD_EPISODE_ID)
        verify(mListenerMock2).onOldPodcastDetected(OLD_EPISODE_ID)
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeSuccessOldPodcastDected_unsubscribedListenerNotNotified() {
        // Arrange
        isOldPodcast()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onOldPodcastDetected(OLD_EPISODE_ID)
        verify(mListenerMock2, never()).onOldPodcastDetected(OLD_EPISODE_ID)
    }

    @Test
    fun detectOldOrNewPodcastAndNotify_restoreEpisodeError_listenersNotifiedOfError() {
        // Arrange
        restoreEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.detectOldOrNewPodcastAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onPodcastDetectFailed()
        verify(mListenerMock1).onPodcastDetectFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun togglePlayButtonAndNotify_isOldPodcastNull_notifyToggleOldEpisodePlaybackFailed() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleOldEpisodePlaybackFailed()
        verify(mListenerMock2).onToggleOldEpisodePlaybackFailed()
    }

    @Test
    fun togglePlayButtonAndNotify_isOldPodcast_notifyToggleOldEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = true
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleOldEpisodePlayback()
        verify(mListenerMock2).onToggleOldEpisodePlayback()
    }

    @Test
    fun togglePlayButtonAndNotify_currentEpisodeIdNotNull_notifyToggleOldEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = OLD_EPISODE_ID
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleOldEpisodePlayback()
        verify(mListenerMock2).onToggleOldEpisodePlayback()
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcast_correctPodcastIdSentToGetAllPodcastEpisodes() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(mEpisodesCacheForDetailsTd.mGetAllPodcastEpisodesCounter, `is`(1))
        assertThat(
            mEpisodesCacheForDetailsTd.mGetAllPodcastEpisodesArgPodcastId, `is`(
                OLD_PODCAST_ID
            )
        )
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccess_isOldPodcastSetToTrue() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(SUT.isOldPodcast, `is`(true))
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessNoEpisodesAvailable_nullSavedToCurrentEpisodeIdVariable() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        assertNull(SUT.currentEpisodeId)
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessNoEpisodesAvailable_notifyToggleNewEpisodePlaybackFailed() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlaybackFailed()
        verify(mListenerMock2).onToggleNewEpisodePlaybackFailed()
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_currentEpisodeIdSavedToVariable() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        getAllEpisodesEpisodesAvailable()
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(SUT.currentEpisodeId, `is`(NEW_EPISODE_ID))
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_notifyToggleNewEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlayback(NEW_EPISODE_ID)
        verify(mListenerMock2).onToggleNewEpisodePlayback(NEW_EPISODE_ID)
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailableSecondCall_getAllPodcastEpisodesCalledOnlyOnceNotifyToggleOldEpisodePlayback() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        assertThat(mEpisodesCacheForDetailsTd.mGetAllPodcastEpisodesCounter, `is`(1))
        verify(mListenerMock1).onToggleOldEpisodePlayback()
        verify(mListenerMock2).onToggleOldEpisodePlayback()
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesSuccessEpisodesAvailable_unregisteredListenersNotNotified() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        getAllEpisodesEpisodesAvailable()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlayback(NEW_EPISODE_ID)
        verify(mListenerMock2, never()).onToggleNewEpisodePlayback(NEW_EPISODE_ID)
    }

    @Test
    fun togglePlayButtonAndNotify_isNewPodcastGetAllPodcastEpisodesError_notifyToggleNewEpisodePlaybackFailed() {
        // Arrange
        SUT.isOldPodcast = false
        SUT.currentEpisodeId = null
        getAllPodcastEpisodesFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.togglePlayButtonAndNotify(OLD_PODCAST_ID)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlaybackFailed()
        verify(mListenerMock2).onToggleNewEpisodePlaybackFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun toggleListedEpisodePlaybackAndNotify_isOldEpisode_listenersNotifiedOfToggle() {
        // Arrange
        SUT.currentEpisodeId = OLD_EPISODE_ID
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleListedEpisodePlaybackAndNotify(OLD_EPISODE_ID)
        // Assert
        verify(mListenerMock1).onToggleOldEpisodePlayback()
        verify(mListenerMock2).onToggleOldEpisodePlayback()
    }

    @Test
    fun toggleListedEpisodePlaybackAndNotify_isNewEpisode_currentEpisodeIdUpdated() {
        // Arrange
        SUT.currentEpisodeId = null
        // Act
        SUT.toggleListedEpisodePlaybackAndNotify(NEW_EPISODE_ID)
        // Assert
        assertThat(SUT.currentEpisodeId, `is`(NEW_EPISODE_ID))
    }

    @Test
    fun toggleListedEpisodePlaybackAndNotify_isNewEpisode_listenersNotifiedOfToggle() {
        // Arrange
        SUT.currentEpisodeId = OLD_EPISODE_ID
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleListedEpisodePlaybackAndNotify(NEW_EPISODE_ID)
        // Assert
        verify(mListenerMock1).onToggleNewEpisodePlayback(NEW_EPISODE_ID)
        verify(mListenerMock2).onToggleNewEpisodePlayback(NEW_EPISODE_ID)
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun expandListedEpisodeAndNotify_isOldEpisode_listenersNotifiedOfToggle() {
        // Arrange
        SUT.currentEpisodeId = OLD_EPISODE_ID
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.expandListedEpisodeAndNotify(OLD_EPISODE_ID)
        // Assert
        verify(mListenerMock1).onExpandOldListedEpisode()
        verify(mListenerMock2).onExpandOldListedEpisode()
    }

    @Test
    fun expandListedEpisodeAndNotify_isNewEpisode_currentEpisodeIdUpdated() {
        // Arrange
        SUT.currentEpisodeId = null
        // Act
        SUT.expandListedEpisodeAndNotify(NEW_EPISODE_ID)
        // Assert
        assertThat(SUT.currentEpisodeId, `is`(NEW_EPISODE_ID))
    }

    @Test
    fun expandListedEpisodeAndNotify_isNewEpisode_listenersNotifiedOfToggle() {
        // Arrange
        SUT.currentEpisodeId = OLD_EPISODE_ID
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.expandListedEpisodeAndNotify(NEW_EPISODE_ID)
        // Assert
        verify(mListenerMock1).onExpandNewListedEpisode(NEW_EPISODE_ID)
        verify(mListenerMock2).onExpandNewListedEpisode(NEW_EPISODE_ID)
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
        mEpisodesCacheForDetailsTd.mRestoreEpisodeFailure = true
    }

    fun isNotLastPlayedPodcast() {
        // no-op because mIsOldPodcast false by default
    }

    fun isOldPodcast() {
        mEpisodesCacheForDetailsTd.mIsOldPodcast = true
    }

    fun noLastEpisode() {
        mEpisodesCacheForDetailsTd.mIsOldPodcast = null
    }

    fun getAllEpisodesNoEpisodesAvailable() {
        // no-op because mGetAllPodcastEpisodesEpisodesAvailable false by default
    }

    fun getAllEpisodesEpisodesAvailable() {
        mEpisodesCacheForDetailsTd.mGetAllPodcastEpisodesEpisodesAvailable = true
    }

    fun getAllPodcastEpisodesSuccess() {
        // no-op because mGetAllPodcastEpisodesFailure false by default
    }

    fun getAllPodcastEpisodesFailure() {
        mEpisodesCacheForDetailsTd.mGetAllPodcastEpisodesFailure = true
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

    class EpisodesCacheTdForDetailsTd : EpisodesCacheTd() {

        override fun getAllPodcastEpisodes(
            podcastId: String,
            listener: GetAllPodcastEpisodesListener
        ) {
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
                        GET_NEW_EPISODE(),
                        EpisodeMocks.GET_EPISODE_1()
                    )
                )
            }
        }

        var mIsOldPodcast: Boolean? = false
        override fun restoreEpisode(listener: RestoreEpisodeListener) {
            mRestoreEpisodeCounter += 1
            if (!mRestoreEpisodeFailure) {
                if (mIsOldPodcast == null) {
                    listener.onEpisodeRestoreSuccess(null)
                    return
                }
                if (!mIsOldPodcast!!) {
                    val restoredEpisodeMock = GET_NEW_EPISODE()
                    restoredEpisodeMock.podcastId = NEW_PODCAST_ID
                    listener.onEpisodeRestoreSuccess(restoredEpisodeMock)
                } else {
                    val restoredEpisodeMock = GET_OLD_EPISODE()
                    restoredEpisodeMock.podcastId = OLD_PODCAST_ID
                    listener.onEpisodeRestoreSuccess(restoredEpisodeMock)
                }
            } else {
                listener.onEpisodeRestoreFailed()
            }
        }
    }
    // endregion helper classes
}


