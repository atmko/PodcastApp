package com.atmko.skiptoit.subcriptions

import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.testclass.LoginManagerTd
import com.atmko.skiptoit.testclass.PodcastsEndpointTd
import com.atmko.skiptoit.testclass.SubscriptionsCacheTd
import com.atmko.skiptoit.testclass.SubscriptionsEndpointTd
import com.atmko.skiptoit.testdata.PodcastMocks
import com.atmko.skiptoit.testdata.SubscriptionMocks
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
        getRemoteSubscriptionsSuccess()
        getLocalSubscriptionsSuccess()
        endpointSuccess()
        subscriptionsCacheSuccess()
        noPushablePodcasts()
        noPullablePodcasts()
    }

    @Test
    fun silentSignIn_silentSignInSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.silentSignIn()
        // Assert
        verify(mListenerMock1).onSilentSignInSuccess()
        verify(mListenerMock2).onSilentSignInSuccess()
    }

    @Test
    fun silentSignIn_silentSignInSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.silentSignIn()
        // Assert
        verify(mListenerMock1).onSilentSignInSuccess()
        verify(mListenerMock2, never()).onSilentSignInSuccess()
    }

    @Test
    fun silentSignIn_silentSignInError_listenersNotifiedOfSuccess() {
        // Arrange
        silentSignInError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.silentSignIn()
        // Assert
        verify(mListenerMock1).onSilentSignInFailed()
        verify(mListenerMock2).onSilentSignInFailed()
    }

    //----------------------------------------------------------------------------------------------

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
    fun checkSyncStatusAndNotify_checkIsSubscriptionsSyncedCalled() {
        // Arrange
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsSynced_variableUpdated() {
        // Arrange
        SUT.mIsSubscriptionsSynced = null
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsSynced_listenersNotified() {
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
    fun checkSyncStatusAndNotify_subscriptionsSynced_isSubscriptionsSyncedCalledOnlyOnce() {
        // Arrange
        SUT.mIsSubscriptionsSynced = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSynced_getRemoteSubscriptionsCalled() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mGetSubscriptionsCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccess_getLocalSubscriptionsCalled() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsCacheTd.mGetSubscriptionsCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessNoPushablePodcasts_batchSubscribePodcastsNotCalled() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mBatchSubscribeCounter, `is`(0))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessNoPushablePodcasts_setRemoteSubscriptionsSynced() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessNoPushablePodcasts_setSubscriptionsSyncedCalledOnce() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcasts_correctCombinedIdsPassedToBatchSubscribePodcasts() {
        // Assert
        subscriptionsNotSynced()
        pushablePodcasts()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsEndpointTd.mBatchSubscribeCounter, `is`(1))
        assertThat(mSubscriptionsEndpointTd.mBatchSubscribeArgCombinedPocastIds, `is`(SubscriptionMocks.LISTEN_NOTES_ID_3 + "," + SubscriptionMocks.LISTEN_NOTES_ID_4))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsSuccess_syncVariablesUpdated() {
        // Arrange
        subscriptionsNotSynced()
        pushablePodcasts()
        SUT.mIsSubscriptionsSynced = null
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsSuccess_trueArgumentPassedToSetSubscriptionsSynced() {
        // Assert
        subscriptionsNotSynced()
        pushablePodcasts()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(true))
    }


    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsSuccessSetSubscriptionsSyncedSuccess_listenersNotifiedOfSuccess() {
        // Assert
        subscriptionsNotSynced()
        pushablePodcasts()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsSuccessSetSubscriptionsSyncedSuccess_unregisteredListenersNotNotifiedOfSuccess() {
        // Assert
        subscriptionsNotSynced()
        pushablePodcasts()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2, never()).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsSuccessSetSubscriptionsSyncedSuccessSecondCall_isSubscriptionsSyncedCalledOnlyOnce() {
        // Arrange
        subscriptionsNotSynced()
        pushablePodcasts()
        SUT.mIsSubscriptionsSynced = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessNoPullablePodcasts_getBatchPodcastMetaDataNotCalled() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mPodcastsEndpointTd.mGetBatchPodcastMetadataCounter, `is`(0))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessNoPullablePodcasts_setLocalSubscriptionsSynced() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessNoPullablePodcasts_setSubscriptionsSyncedCalledOnce() {
        // Assert
        subscriptionsNotSynced()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcasts_correctCombinedIdsPassedToGetBatchPodcastMetadata() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mPodcastsEndpointTd.mGetBatchPodcastMetadataCounter, `is`(1))
        assertThat(mPodcastsEndpointTd.mCombinedPodcastIds, `is`(SubscriptionMocks.LISTEN_NOTES_ID_1 + "," + SubscriptionMocks.LISTEN_NOTES_ID_2))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccess_correctPodcastsPassedToInsertSubscriptions() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionArgPodcasts,
            `is`(listOf(PodcastMocks.GET_PODCAST_1(), PodcastMocks.GET_PODCAST_2()))
        )
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccess_syncVariablesUpdated() {
        // Arrange
        subscriptionsNotSynced()
        pullablePodcasts()
        SUT.mIsSubscriptionsSynced = null
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccess_trueArgumentPassedToSetSubscriptionsSynced() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccessSetSubscriptionsSyncedSuccess_listenersNotifiedOfSuccess() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccessSetSubscriptionsSyncedSuccess_unregisteredListenersNotNotifiedOfSuccess() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSynced()
        verify(mListenerMock2, never()).onSubscriptionsSyncStatusSynced()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccesPullablePodcastssGetBatchPodcastMetadataSuccessInsertSubscriptionsSuccessSetSubscriptionsSyncedSuccessSecondCall_isSubscriptionsSyncedCalledOnlyOnce() {
        // Arrange
        subscriptionsNotSynced()
        pullablePodcasts()
        SUT.mIsSubscriptionsSynced = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsSubscriptionsSyncedCounter, `is`(1))
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(true))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(true))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsError_falseArgumentPassedIntoUpdateMethod() {
        // Arrange
        subscriptionsNotSynced()
        getRemoteSubscriptionsError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(false))
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsError_listenersNotifiedOfError() {
        // Arrange
        subscriptionsNotSynced()
        getRemoteSubscriptionsError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsError_falseArgumentPassedIntoUpdateMethod() {
        // Arrange
        subscriptionsNotSynced()
        getLocalSubscriptionsError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(false))
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsError_listenersNotifiedOfError() {
        // Assert
        subscriptionsNotSynced()
        getLocalSubscriptionsError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsError_falseArgumentPassedIntoUpdateMethod() {
        // Arrange
        subscriptionsNotSynced()
        pushablePodcasts()
        batchSubscribePodcastsError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(false))
        assertThat(SUT.mIsRemoteSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPushablePodcastsBatchSubscribePodcastsError_listenersNotifiedOfError() {
        // Arrange
        subscriptionsNotSynced()
        pushablePodcasts()
        batchSubscribePodcastsError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataError_falseArgumentPassedIntoUpdateMethod() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        getBatchPodcastDataError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(false))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataError_listenersNotifiedOfError() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        getBatchPodcastDataError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(false))
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccessInsertSubscriptionsError_falseArgumentPassedIntoUpdateMethod() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        insertSubscriptionError()
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(false))
        assertThat(SUT.mIsLocalSubscriptionsSynced, `is`(false))
        assertThat(SUT.mIsSubscriptionsSynced, `is`(false))
    }

    @Test
    fun checkSyncStatusAndNotify_subscriptionsNotSyncedGetRemoteSubscriptionsSuccessGetLocalSubscriptionsSuccessPullablePodcastsGetBatchPodcastMetadataSuccessInsertSubscriptionsError_listenersNotifiedOfError() {
        // Assert
        subscriptionsNotSynced()
        pullablePodcasts()
        insertSubscriptionError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.checkSyncStatusAndNotify()
        // Assert
        verify(mListenerMock1).onSubscriptionsSyncStatusSyncFailed()
        verify(mListenerMock2).onSubscriptionsSyncStatusSyncFailed()
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
        mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced = false
    }

    fun getRemoteSubscriptionsSuccess() {
        // no-op because mGetSubscriptionsFailure false by default
    }

    fun getRemoteSubscriptionsError() {
        mSubscriptionsEndpointTd.mGetSubscriptionsFailure = true
    }

    fun getLocalSubscriptionsSuccess() {
        // no-op because mGetSubscriptionsFailure false by default
    }

    fun getLocalSubscriptionsError() {
        mSubscriptionsCacheTd.mGetSubscriptionsFailure = true
    }

    fun batchSubscribePodcastsSuccess() {
        // no-op because mBatchSubscribePodcastsFailure false by default
    }

    fun batchSubscribePodcastsError() {
        mSubscriptionsEndpointTd.mBatchSubscribePodcastsFailure = true
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

    private fun noPushablePodcasts() {
        // no-op because mNoPushablePodcasts true by default
    }

    private fun pushablePodcasts() {
        mSubscriptionsCacheTd.mNoPushablePodcasts = false
    }

    private fun noPullablePodcasts() {
        // no-op because mNoPullablePodcasts true by default
    }

    private fun pullablePodcasts() {
        mSubscriptionsEndpointTd.mNoPullablePodcasts = false
    }
    // endregion helper methods

    // region helper classes
    // endregion helper classes
}