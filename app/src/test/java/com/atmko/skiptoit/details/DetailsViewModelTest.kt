package com.atmko.skiptoit.details

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.subcriptions.STATUS_SUBSCRIBE
import com.atmko.skiptoit.subcriptions.STATUS_UNSUBSCRIBE
import com.atmko.skiptoit.testclass.SubscriptionsCacheTd
import com.atmko.skiptoit.testclass.SubscriptionsEndpointTd
import com.atmko.skiptoit.testdata.PodcastMocks
import com.atmko.skiptoit.testutils.TestUtils
import com.atmko.skiptoit.testutils.kotlinCapture
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DetailsViewModelTest {

    // region constants

    // endregion constants

    // end region helper fields
    lateinit var mPodcastDetailsEndpointTd: PodcastDetailsEndpointTd
    lateinit var mSubscriptionsEndpointTd: SubscriptionsEndpointTd
    lateinit var mSubscriptionsCacheTd: SubscriptionsCacheTd

    @Mock lateinit var mListenerMock1: DetailsViewModel.Listener
    @Mock lateinit var mListenerMock2: DetailsViewModel.Listener

    @Captor lateinit var mArgCaptorPodcastDetails: ArgumentCaptor<PodcastDetails>
    // endregion helper fields

    lateinit var SUT: DetailsViewModel

    @Before
    fun setup() {
        mPodcastDetailsEndpointTd = PodcastDetailsEndpointTd()
        mSubscriptionsEndpointTd = SubscriptionsEndpointTd()
        mSubscriptionsCacheTd = SubscriptionsCacheTd()
        SUT = DetailsViewModel(
            mPodcastDetailsEndpointTd,
            mSubscriptionsEndpointTd,
            mSubscriptionsCacheTd
        )
        detailsEndpointSuccess()
        subscriptionsEndpointSuccess()
        subscriptionCacheSuccess()
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
    fun loadSubscriptionStatusAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun loadSubscriptionStatusAndNotify_correctPodcastIdPassedToCache() {
        // Arrange
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mSubscriptionsCacheTd.mGetSubscriptionStatusCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
    }

    @Test
    fun loadSubscriptionStatusAndNotify_subscriptionCacheSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mSubscriptionsCacheTd.mGetSubscriptionStatusCounter, `is`(1))
        assertThat(SUT.isSubscribed, `is`(true))
    }

    @Test
    fun loadSubscriptionStatusAndNotify_subscriptionCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac : ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onStatusFetched(ac.capture())
        verify(mListenerMock2).onStatusFetched(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(true))
        assertThat(captures[1], `is`(true))
    }

    @Test
    fun loadSubscriptionStatusAndNotify_subscriptionCacheSuccess_useCachedValueUponSecondCall() {
        // Arrange
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        assertThat(mSubscriptionsCacheTd.mGetSubscriptionStatusCounter, `is`(1))
        assertThat(SUT.isSubscribed, `is`(true))
    }

    @Test
    fun loadSubscriptionStatusAndNotify_subscriptionCacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onStatusFetched(TestUtils.kotlinAny(Boolean::class.java))
        verify(mListenerMock2, never()).onStatusFetched(TestUtils.kotlinAny(Boolean::class.java))
    }

    @Test
    fun loadSubscriptionStatusAndNotify_subscriptionCacheError_listenersNotifiedOfError() {
        // Arrange
        subscriptionCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.loadSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mListenerMock1).onStatusFetchFailed()
        verify(mListenerMock2).onStatusFetchFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun toggleSubscriptionAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        isSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubScribed_correctPodcastIdAndSubscriptionStatusPassedToEndpoint() {
        // Arrange
        isSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsEndpointTd.mUpdateSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsEndpointTd.mPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
        assertThat(mSubscriptionsEndpointTd.mSubscriptionStatus, `is`(STATUS_UNSUBSCRIBE))
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubScribedSubscriptionEndpointSuccess_correctPodcastIdPassedToCache() {
        // Arrange
        isSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubScribedSubscriptionEndpointSuccessCacheUpdateSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        isSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        assertThat(SUT.isSubscribed, `is`(false))
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionEndpointSuccessCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(ac.capture())
        verify(mListenerMock2).onStatusUpdated(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(false))
        assertThat(captures[1], `is`(false))
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionEndpointSuccessCacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(false)
        verify(mListenerMock2, never()).onStatusUpdated(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionsEndPointError_listenersNotifiedOfError() {
        // Arrange
        isSubscribed()
        subscriptionsEndpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsEndpointTd.mUpdateSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(0))
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock1).onStatusUpdateFailed()
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionEndpointSuccessCacheError_listenersNotifiedOfError() {
        // Arrange
        isSubscribed()
        subscriptionCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock1).onStatusUpdateFailed()
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubScribed_correctPodcastIdAndSubscriptionStatusPassedToEndpoint() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsEndpointTd.mUpdateSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsEndpointTd.mPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
        assertThat(mSubscriptionsEndpointTd.mSubscriptionStatus, `is`(STATUS_SUBSCRIBE))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubScribedSubscriptionEndpointSuccess_correctPodcastIdPassedToCache() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mPodcasts[0], `is`(PodcastMocks.GET_PODCAST_1()))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubScribedSubscriptionEndpointSuccessCacheUpdateSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(SUT.isSubscribed, `is`(true))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionEndpointSuccessCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isNotSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(ac.capture())
        verify(mListenerMock2).onStatusUpdated(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(true))
        assertThat(captures[1], `is`(true))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionEndpointSuccessCacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isNotSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(true)
        verify(mListenerMock2, never()).onStatusUpdated(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionsEndPointError_listenersNotifiedOfError() {
        // Arrange
        isNotSubscribed()
        subscriptionsEndpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsEndpointTd.mUpdateSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(0))
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock1).onStatusUpdateFailed()
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionEndpointSuccessCacheError_listenersNotifiedOfError() {
        // Arrange
        isNotSubscribed()
        subscriptionCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock1).onStatusUpdateFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun toggleLocalSubscriptionAndNotify_correctPodcastIdPassedToCache() {
        // Arrange
        isSubscribed()
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mPodcastId, `is`(PodcastMocks.PODCAST_ID_1))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheUpdateSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        isSubscribed()
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(SUT.isSubscribed, `is`(false))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(ac.capture())
        verify(mListenerMock2).onStatusUpdated(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(false))
        assertThat(captures[1], `is`(false))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(false)
        verify(mListenerMock2, never()).onStatusUpdated(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheError_listenersNotifiedOfError() {
        // Arrange
        isSubscribed()
        subscriptionCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock1).onStatusUpdateFailed()
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubScribed_correctPodcastIdPassedToCache() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mPodcasts[0], `is`(PodcastMocks.GET_PODCAST_1()))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubScribedCacheUpdateSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(SUT.isSubscribed, `is`(true))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubscribedCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isNotSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(ac.capture())
        verify(mListenerMock2).onStatusUpdated(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(true))
        assertThat(captures[1], `is`(true))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubscribedCacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isNotSubscribed()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mListenerMock1).onStatusUpdated(true)
        verify(mListenerMock2, never()).onStatusUpdated(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubscribedCacheError_listenersNotifiedOfError() {
        // Arrange
        isNotSubscribed()
        subscriptionCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock1).onStatusUpdateFailed()
    }

    // region helper methods
    fun detailsEndpointSuccess() {
        // no-op because mFailure false by default
    }

    fun detailsEndpointError() {
        mPodcastDetailsEndpointTd.mFailure = true
    }

    fun subscriptionsEndpointSuccess() {
        // no-op because mFailure false by default
    }

    fun subscriptionsEndpointError() {
        mSubscriptionsEndpointTd.mFailure = true
    }

    fun subscriptionCacheSuccess() {
        // no-op because mFailure false by default
    }

    fun subscriptionCacheError() {
        mSubscriptionsCacheTd.mFailure = true
    }

    fun isSubscribed() {
        SUT.isSubscribed = true
    }

    fun isNotSubscribed() {
        SUT.isSubscribed = false
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


