package com.atmko.skiptoit.details

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.testdata.PodcastMocks
import com.atmko.skiptoit.testutils.TestUtils
import com.atmko.skiptoit.testutils.kotlinCapture
import org.hamcrest.CoreMatchers.`is`
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

    // endregion constants

    // end region helper fields
    lateinit var mPodcastDetailsEndpointTd: PodcastDetailsEndpointTd

    @Mock lateinit var mListenerMock1: DetailsViewModel.Listener
    @Mock lateinit var mListenerMock2: DetailsViewModel.Listener

    @Captor lateinit var mArgCaptorPodcastDetails: ArgumentCaptor<PodcastDetails>
    // endregion helper fields

    lateinit var SUT: DetailsViewModel

    @Before
    fun setup() {
        mPodcastDetailsEndpointTd = PodcastDetailsEndpointTd()
        SUT = DetailsViewModel(
            mPodcastDetailsEndpointTd
        )
        detailsEndpointSuccess()
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

    // region helper methods
    fun detailsEndpointSuccess() {
        // no-op because mFailure false by default
    }

    fun detailsEndpointError() {
        mPodcastDetailsEndpointTd.mFailure = true
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


