package com.atmko.skiptoit.subcriptions

import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.testclass.LoginManagerTd
import com.atmko.skiptoit.testclass.PodcastsEndpointTd
import com.atmko.skiptoit.testclass.SubscriptionsCacheTd
import com.atmko.skiptoit.testclass.SubscriptionsEndpointTd
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
        @Mock lateinit var GOOGLE_SIGN_IN_ACCOUNT_MOCK: GoogleSignInAccount
        const val PODCAST_ID = "podcastId"
    }

    // endregion constants

    // end region helper fields
    lateinit var mLoginManagerTd: LoginManagerTd
    lateinit var mPodcastsEndpointTd: PodcastsEndpointTd
    lateinit var mSubscriptionsEndpointTd: SubscriptionsEndpointTd
    lateinit var mSubscriptionsCacheTd: SubscriptionsCacheTd
    @Mock lateinit var mSubscriptionsDao: SubscriptionsDao

    @Mock lateinit var mListenerMock1: SubscriptionsViewModel.Listener
    @Mock lateinit var mListenerMock2: SubscriptionsViewModel.Listener
    // endregion helper fields

    lateinit var SUT: SubscriptionsViewModel

    @Before
    fun setup() {
        mLoginManagerTd = LoginManagerTd()
        mPodcastsEndpointTd = PodcastsEndpointTd()
        mSubscriptionsEndpointTd = SubscriptionsEndpointTd()
        mSubscriptionsCacheTd = SubscriptionsCacheTd()
        SUT = SubscriptionsViewModel(mLoginManagerTd, mPodcastsEndpointTd,
            mSubscriptionsEndpointTd, mSubscriptionsCacheTd, mSubscriptionsDao)

        mLoginManagerTd.mGoogleSignInAccount = GOOGLE_SIGN_IN_ACCOUNT_MOCK
        silentSignInSuccess()
        subscriptionsSynced()
        getSubscriptionsSuccess()
        endpointSuccess()
        subscriptionsCacheSuccess()
    }

    @Test
    fun checkSyncStatusAndNotify_notifyProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInCalled() {
        // Arrange
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccess_checkIsSubscriptionsSyncedCalled() {
        // Arrange
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInError_nothingNotified() {
        // Arrange
        silentSignInError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1, never()).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2, never()).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock1, never()).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2, never()).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsSynced_variableUpdated() {
        // Arrange
        SUT.mIsSubscriptionsSynced = null
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsSynced_listenersNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsSynced_silentSignInCalledOnlyOnce() {
        // Arrange
        SUT.mIsSubscriptionsSynced = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(1))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSynced_restoreSubscriptionsCalled() {
        // Arrange
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsError_falseArgumentPassedIntoUpdateMethod() {
        // Arrange
        subscriptionsNotSynced()
        getSubscriptionsError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsError_listenersNotifiedOfError() {
        // Arrange
        subscriptionsNotSynced()
        getSubscriptionsError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataError_falseArgumentPassedIntoUpdateMethod() {
        // Assert
        subscriptionsNotSynced()
        getBatchPodcastDataError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataError_listenersNotifiedOfError() {
        // Assert
        subscriptionsNotSynced()
        getBatchPodcastDataError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        assertThat(mPodcastsEndpointTd.mGetBatchPodcastMetadataCounter, `is`(1))
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsError_falseArgumentPassedIntoUpdateMethod() {
        // Assert
        subscriptionsNotSynced()
        insertSubscriptionError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsError_listenersNotifiedOfError() {
        // Assert
        subscriptionsNotSynced()
        insertSubscriptionError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        assertThat(mPodcastsEndpointTd.mGetBatchPodcastMetadataCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccess_trueArgumentPassedIntoUpdateMethod() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccess_listenersNotifiedOfSuccess() {
        // Assert
        subscriptionsNotSynced()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        assertThat(mPodcastsEndpointTd.mGetBatchPodcastMetadataCounter, `is`(1))
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccess_unregisteredListenersNotNotifiedOfSuccess() {
        // Assert
        subscriptionsNotSynced()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
        assertThat(mPodcastsEndpointTd.mGetBatchPodcastMetadataCounter, `is`(1))
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2, never()).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccess_variableUpdated() {
        // Arrange
        subscriptionsNotSynced()
        SUT.mIsSubscriptionsSynced = null
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_silentSignInSuccessSubscriptionsNotSyncedGetSubscriptionsSuccessGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccessSecondCall_silentSignInCalledOnlyOnce() {
        // Arrange
        subscriptionsNotSynced()
        SUT.mIsSubscriptionsSynced = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(1))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun unsubscribeAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
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
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdated()
        verify(mListenerMock2).onStatusUpdated()
    }

    @Test
    fun unsubscribeAndNotify_endpointSuccessCacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdated()
        verify(mListenerMock2, never()).onStatusUpdated()
    }

    @Test
    fun unsubscribeAndNotify_endpointSuccessCacheError_listenersNotifiedOfFailure() {
        // Arrange
        subscriptionsCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock2).onStatusUpdateFailed()
    }

    @Test
    fun unsubscribeAndNotify_endpointErrorCacheError_listenersNotifiedOfFailure() {
        // Arrange
        endpointError()
        subscriptionsCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.unsubscribeAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock2).onStatusUpdateFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun unsubscribeLocallyAndNotify_correctPodcastIdPassedToCache() {
        // Arrange
        // Act
        SUT.unsubscribeLocallyAndNotify(PODCAST_ID)
        // Assert
        assertThat(mSubscriptionsCacheTd.mPodcastId, `is`(PODCAST_ID))
    }

    @Test
    fun unsubscribeLocallyAndNotify_cacheSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.unsubscribeLocallyAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdated()
        verify(mListenerMock2).onStatusUpdated()
    }

    @Test
    fun unsubscribeLocallyAndNotify_cacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.unsubscribeLocallyAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdated()
        verify(mListenerMock2, never()).onStatusUpdated()
    }

    @Test
    fun unsubscribeLocallyAndNotify_cacheError_listenersNotifiedOfFailure() {
        // Arrange
        subscriptionsCacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.unsubscribeLocallyAndNotify(PODCAST_ID)
        // Assert
        verify(mListenerMock1).onStatusUpdateFailed()
        verify(mListenerMock2).onStatusUpdateFailed()
    }

    // region helper methods
    fun silentSignInSuccess() {
        // no-op because mSilentSignInFailure false by default
    }

    fun silentSignInError() {
        mLoginManagerTd.mSilentSignInFailure =  true
    }

    private fun subscriptionsSynced() {
        // no-op because mIsSubscriptionsSynced true by default
    }

    private fun subscriptionsNotSynced() {
        mLoginManagerTd.mIsSubscriptionsSynced = false
    }

    fun getSubscriptionsSuccess() {
        // no-op because mGetSubscriptionsFailure false by default
    }

    fun getSubscriptionsError() {
        mSubscriptionsEndpointTd.mGetSubscriptionsFailure = true
    }

    fun getBatchPodcastDataSuccess() {
        // no-op because mGetBatchPodcastMetadataFailure false by default
    }

    fun getBatchPodcastDataError() {
        mPodcastsEndpointTd.mGetBatchPodcastMetadataFailure = true
    }

    fun insertSubscriptionSuccess() {
        // no-op because mFailure false by default
    }

    fun insertSubscriptionError() {
        mSubscriptionsCacheTd.mFailure = true
    }

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