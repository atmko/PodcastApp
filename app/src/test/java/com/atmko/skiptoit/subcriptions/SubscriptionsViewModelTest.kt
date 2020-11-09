package com.atmko.skiptoit.subcriptions

import com.atmko.skiptoit.testclass.LoginManagerTd
import com.atmko.skiptoit.testclass.PodcastsEndpointTd
import com.atmko.skiptoit.testclass.SubscriptionsCacheTd
import com.atmko.skiptoit.testclass.SubscriptionsEndpointTd
import com.atmko.skiptoit.testdata.PodcastMocks
import com.atmko.skiptoit.testdata.SubscriptionMocks
import com.atmko.skiptoit.testutils.TestUtils
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SubscriptionsViewModelTest {

    // region constants
    companion object {
        @Mock lateinit var GOOGLE_SIGN_IN_ACCOUNT_MOCK: GoogleSignInAccount
    }

    // endregion constants

    // end region helper fields
    lateinit var mLoginManagerTd: LoginManagerTd
    lateinit var mPodcastsEndpointTd: PodcastsEndpointTd
    lateinit var mSubscriptionsEndpointTd: SubscriptionsEndpointTd
    lateinit var mSubscriptionsCacheTd: SubscriptionsCacheTd

    @Mock lateinit var mListenerMock1: SubscriptionsViewModel.Listener
    @Mock lateinit var mListenerMock2: SubscriptionsViewModel.Listener

    @Mock lateinit var mSubscriptionStatusListenerMock1: SubscriptionsViewModel.FetchSubscriptionStatusListener
    @Mock lateinit var mSubscriptionStatusListenerMock2: SubscriptionsViewModel.FetchSubscriptionStatusListener

    @Mock lateinit var mToggleListenerMock1: SubscriptionsViewModel.ToggleSubscriptionListener
    @Mock lateinit var mToggleListenerMock2: SubscriptionsViewModel.ToggleSubscriptionListener
    // endregion helper fields

    lateinit var SUT: SubscriptionsViewModel

    @Before
    fun setup() {
        mLoginManagerTd = LoginManagerTd()
        mPodcastsEndpointTd = PodcastsEndpointTd()
        mSubscriptionsEndpointTd = SubscriptionsEndpointTd()
        mSubscriptionsCacheTd = SubscriptionsCacheTd()
        SUT = SubscriptionsViewModel(mLoginManagerTd, mPodcastsEndpointTd,
            mSubscriptionsEndpointTd, mSubscriptionsCacheTd)

        mLoginManagerTd.mGoogleSignInAccount = GOOGLE_SIGN_IN_ACCOUNT_MOCK
        silentSignInSuccess()
        subscriptionsSynced()
        getRemoteSubscriptionsSuccess()
        getLocalSubscriptionsSuccess()
        subscriptionsCacheSuccess()
        noPushablePodcasts()
        noPullablePodcasts()

        subscriptionStatusFetchSuccess()
        updateServerSubscriptionSuccess()
        insertSubscriptionSuccess()
        removeSubscriptionSuccess()
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

    // ---------------------------------------------------------------------------------------------

    @Test
    fun getSubscriptions_getSubscriptionsLiveDataCalled() {
        // Assert
        // Act
        SUT.getSubscriptions()
        // Assert
        assertThat(mSubscriptionsCacheTd.mGetSubscriptionsLiveDataCounter, `is`(1))
    }

    @Test
    fun getSubscriptions_getSubscriptionsLiveDataSuccess_subscriptionsSavedToVariable() {
        // Assert
        // Act
        SUT.getSubscriptions()
        // Assert
        assertThat(SUT.subscriptions!!.value, `is`(PodcastMocks.PodcastLiveDataMocks.GET_PODCAST_LIST().value))
    }

    @Test
    fun saveSubscriptionMap_getSubscriptionsLiveDataSuccess_subscriptionsSavedToVariable() {
        // Assert
        // Act
        SUT.saveSubscriptionMap(listOf(PodcastMocks.GET_PODCAST_1(), PodcastMocks.GET_PODCAST_2()))
        // Assert
        assertThat(SUT.subscriptionsMap, `is`(PodcastMocks.PodcastSubscriptionAMap.GET_SUBSCRIPTION_MAP()))
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun loadSubscriptionStatusAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock1)
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock2)
        // Act
        SUT.getSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mSubscriptionStatusListenerMock1).notifyProcessing()
        verify(mSubscriptionStatusListenerMock2).notifyProcessing()
    }

    @Test
    fun getSubscriptionStatusAndNotify_subscriptionCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isSubscribed()
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock1)
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock2)
        val ac : ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.getSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mSubscriptionStatusListenerMock1).onSubscriptionStatusFetched(ac.capture())
        verify(mSubscriptionStatusListenerMock2).onSubscriptionStatusFetched(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(true))
        assertThat(captures[1], `is`(true))
    }

    @Test
    fun getSubscriptionStatusAndNotify_subscriptionCacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock1)
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock2)
        SUT.unregisterSubscriptionStatusListener(mSubscriptionStatusListenerMock2)
        // Act
        SUT.getSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mSubscriptionStatusListenerMock1).onSubscriptionStatusFetched(TestUtils.kotlinAny(Boolean::class.java))
        verify(mSubscriptionStatusListenerMock2, never()).onSubscriptionStatusFetched(TestUtils.kotlinAny(Boolean::class.java))
    }

    @Test
    fun getSubscriptionStatusAndNotify_subscriptionCacheError_listenersNotifiedOfError() {
        // Arrange
        subscriptionStatusFetchError()
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock1)
        SUT.registerSubscriptionStatusListener(mSubscriptionStatusListenerMock2)
        // Act
        SUT.getSubscriptionStatusAndNotify(PodcastMocks.PODCAST_ID_1)
        // Assert
        verify(mSubscriptionStatusListenerMock1).onSubscriptionStatusFetchFailed()
        verify(mSubscriptionStatusListenerMock2).onSubscriptionStatusFetchFailed()
    }

    //----------------------------------------------------------------------------------------------
    @Test
    fun toggleSubscriptionAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        isSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).notifyProcessing()
        verify(mToggleListenerMock2).notifyProcessing()
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
        assertThat(SUT.subscriptionsMap!!.contains(PodcastMocks.GET_PODCAST_1().id), `is`(false))
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionEndpointSuccessCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(ac.capture())
        verify(mToggleListenerMock2).onSubscriptionToggleSuccess(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(false))
        assertThat(captures[1], `is`(false))
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionEndpointSuccessCacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        SUT.unregisterToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(false)
        verify(mToggleListenerMock2, never()).onSubscriptionToggleSuccess(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionsEndPointError_listenersNotifiedOfError() {
        // Arrange
        isSubscribed()
        updateServerSubscriptionError()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsEndpointTd.mUpdateSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(0))
        verify(mToggleListenerMock1).onSubscriptionToggleFailed()
        verify(mToggleListenerMock2).onSubscriptionToggleFailed()
    }

    @Test
    fun toggleSubscriptionAndNotify_isSubscribedSubscriptionEndpointSuccessCacheError_listenersNotifiedOfError() {
        // Arrange
        isSubscribed()
        removeSubscriptionError()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        verify(mToggleListenerMock1).onSubscriptionToggleFailed()
        verify(mToggleListenerMock2).onSubscriptionToggleFailed()
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
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionArgPodcasts[0], `is`(PodcastMocks.GET_PODCAST_1()))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubScribedSubscriptionEndpointSuccessCacheUpdateSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(SUT.subscriptionsMap!!.contains(PodcastMocks.GET_PODCAST_1().id), `is`(true))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionEndpointSuccessCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isNotSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(ac.capture())
        verify(mToggleListenerMock2).onSubscriptionToggleSuccess(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(true))
        assertThat(captures[1], `is`(true))
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionEndpointSuccessCacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isNotSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        SUT.unregisterToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(true)
        verify(mToggleListenerMock2, never()).onSubscriptionToggleSuccess(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionsEndPointError_listenersNotifiedOfError() {
        // Arrange
        isNotSubscribed()
        updateServerSubscriptionError()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsEndpointTd.mUpdateSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(0))
        verify(mToggleListenerMock1).onSubscriptionToggleFailed()
        verify(mToggleListenerMock2).onSubscriptionToggleFailed()
    }

    @Test
    fun toggleSubscriptionAndNotify_isNotSubscribedSubscriptionEndpointSuccessCacheError_listenersNotifiedOfError() {
        // Arrange
        isNotSubscribed()
        insertSubscriptionError()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        verify(mToggleListenerMock1).onSubscriptionToggleFailed()
        verify(mToggleListenerMock2).onSubscriptionToggleFailed()
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
        assertThat(SUT.subscriptionsMap!!.contains(PodcastMocks.GET_PODCAST_1().id), `is`(false))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(ac.capture())
        verify(mToggleListenerMock2).onSubscriptionToggleSuccess(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(false))
        assertThat(captures[1], `is`(false))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        SUT.unregisterToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(false)
        verify(mToggleListenerMock2, never()).onSubscriptionToggleSuccess(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_CacheError_listenersNotifiedOfError() {
        // Arrange
        isSubscribed()
        removeSubscriptionError()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mRemoveSubscriptionCounter, `is`(1))
        verify(mToggleListenerMock1).onSubscriptionToggleFailed()
        verify(mToggleListenerMock2).onSubscriptionToggleFailed()
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubScribed_correctPodcastIdPassedToCache() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionArgPodcasts[0], `is`(PodcastMocks.GET_PODCAST_1()))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubScribedCacheUpdateSuccess_subscriptionStatusSavedInViewModel() {
        // Arrange
        isNotSubscribed()
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        assertThat(SUT.subscriptionsMap!!.contains(PodcastMocks.GET_PODCAST_1().id), `is`(true))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubscribedCacheSuccess_listenersNotifiedOfSuccessWithCorrectValue() {
        // Arrange
        isNotSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        val ac: ArgumentCaptor<Boolean> = ArgumentCaptor.forClass(Boolean::class.java)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(ac.capture())
        verify(mToggleListenerMock2).onSubscriptionToggleSuccess(ac.capture())
        val captures: List<Boolean> = ac.allValues
        assertThat(captures[0], `is`(true))
        assertThat(captures[1], `is`(true))
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubscribedCacheSuccess_unregisteredListenerNotNotified() {
        // Arrange
        isNotSubscribed()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        SUT.unregisterToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        verify(mToggleListenerMock1).onSubscriptionToggleSuccess(true)
        verify(mToggleListenerMock2, never()).onSubscriptionToggleSuccess(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun toggleLocalSubscriptionAndNotify_isNotSubscribedCacheError_listenersNotifiedOfError() {
        // Arrange
        isNotSubscribed()
        insertSubscriptionError()
        SUT.registerToggleListener(mToggleListenerMock1)
        SUT.registerToggleListener(mToggleListenerMock2)
        // Act
        SUT.toggleLocalSubscriptionAndNotify(PodcastMocks.GET_PODCAST_1())
        // Assert
        assertThat(mSubscriptionsCacheTd.mInsertSubscriptionCounter, `is`(1))
        verify(mToggleListenerMock1).onSubscriptionToggleFailed()
        verify(mToggleListenerMock2).onSubscriptionToggleFailed()
    }

    //----------------------------------------------------------------------------------------------

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

    fun subscriptionStatusFetchSuccess() {
        SUT.subscriptionsMap = HashMap()
    }

    fun subscriptionStatusFetchError() {
        SUT.subscriptionsMap = null
    }

    fun updateServerSubscriptionSuccess() {
        // no-op because mUpdateServerSubscriptionFailure false by default
    }

    fun updateServerSubscriptionError() {
        mSubscriptionsEndpointTd.mUpdateServerSubscriptionFailure = true
    }

    fun insertSubscriptionSuccess() {
        // no-op because mFailure false by default
    }

    fun insertSubscriptionError() {
        mSubscriptionsCacheTd.mFailure = true
    }

    fun removeSubscriptionSuccess() {
        // no-op because mRemoveSubscriptionFailure false by default
    }

    fun removeSubscriptionError() {
        mSubscriptionsCacheTd.mRemoveSubscriptionFailure = true
    }

    fun isSubscribed() {
        SUT.subscriptionsMap = HashMap()
        SUT.subscriptionsMap!![PodcastMocks.GET_PODCAST_1().id] = null
    }

    fun isNotSubscribed() {
        SUT.subscriptionsMap = HashMap()
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