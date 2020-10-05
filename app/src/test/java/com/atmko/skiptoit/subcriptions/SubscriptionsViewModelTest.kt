package com.atmko.skiptoit.subcriptions

import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.testclass.SubscriptionsCacheTd
import com.atmko.skiptoit.testclass.SubscriptionsEndpointTd
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SubscriptionsViewModelTest {

    // region constants
    companion object {
        const val PODCAST_ID = "podcastId"
    }

    // endregion constants

    // end region helper fields
    lateinit var mSubscriptionsEndpointTd: SubscriptionsEndpointTd
    lateinit var mSubscriptionsCacheTd: SubscriptionsCacheTd
    @Mock lateinit var mSubscriptionsDao: SubscriptionsDao

    @Mock lateinit var mListener1: SubscriptionsViewModel.Listener
    @Mock lateinit var mListener2: SubscriptionsViewModel.Listener
    // endregion helper fields

    lateinit var SUT: SubscriptionsViewModel

    @Before
    fun setup() {
        mSubscriptionsEndpointTd = SubscriptionsEndpointTd()
        mSubscriptionsCacheTd = SubscriptionsCacheTd()
        SUT = SubscriptionsViewModel(mSubscriptionsEndpointTd, mSubscriptionsCacheTd, mSubscriptionsDao)

        endpointSuccess()
        subscriptionsCacheSuccess()
    }

    @Test
    fun unsubscribeAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListener1)
        SUT.registerListener(mListener2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListener1).notifyProcessing()
        verify(mListener2).notifyProcessing()
    }

    @Test
    fun unsubscribeAndNotify_correctPodcastIdPassedToEndpoint() {
        // Arrange
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        assertThat(mSubscriptionsEndpointTd.mPodcastId, `is`(PODCAST_ID))
    }

    @Test
    fun unsubscribeAndNotify_correctPodcastIdPassedToCache() {
        // Arrange
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        assertThat(mSubscriptionsCacheTd.mPodcastId, `is`(PODCAST_ID))
    }

    @Test
    fun unsubscribeAndNotify_endpointSuccessCacheSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListener1)
        SUT.registerListener(mListener2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListener1).onStatusUpdated()
        verify(mListener2).onStatusUpdated()
    }

    @Test
    fun unsubscribeAndNotify_endpointSuccessCacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListener1)
        SUT.registerListener(mListener2)
        SUT.unregisterListener(mListener2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListener1).onStatusUpdated()
        verify(mListener2, never()).onStatusUpdated()
    }

    @Test
    fun unsubscribeAndNotify_endpointSuccessCacheError_listenersNotifiedOfFailure() {
        // Arrange
        subscriptionsCacheError()
        SUT.registerListener(mListener1)
        SUT.registerListener(mListener2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListener1).onStatusUpdateFailed()
        verify(mListener2).onStatusUpdateFailed()
    }

    @Test
    fun unsubscribeAndNotify_endpointErrorCacheError_listenersNotifiedOfFailure() {
        // Arrange
        endpointError()
        subscriptionsCacheError()
        SUT.registerListener(mListener1)
        SUT.registerListener(mListener2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListener1).onStatusUpdateFailed()
        verify(mListener2).onStatusUpdateFailed()
    }

    // region helper methods
    private fun endpointSuccess() {
        // no-op because mFailure false by default
    }

    private fun endpointError() {
        mSubscriptionsEndpointTd.mFailure = true
    }

    private fun subscriptionsCacheSuccess() {
        // no-op because mFailure false by default
    }

    private fun subscriptionsCacheError() {
        mSubscriptionsCacheTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    // endregion helper classes
}