package com.atmko.skiptoit.common

import android.app.Activity
import android.content.Intent
import com.atmko.skiptoit.UserEndpoint
import com.atmko.skiptoit.testclass.EpisodesCacheTd
import com.atmko.skiptoit.testclass.LoginManagerTd
import com.atmko.skiptoit.testdata.UserMocks
import com.atmko.skiptoit.testutils.TestUtils
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ManagerViewModelTest {

    // region constants
    companion object {
        @Mock
        lateinit var GOOGLE_SIGN_IN_ACCOUNT_MOCK: GoogleSignInAccount

        @Mock
        lateinit var INTENT_MOCK: Intent
        const val REQUEST_CODE_NOT_SIGN_IN = 0
    }
    // endregion constants

    // end region helper fields
    private lateinit var mLoginManagerTd: LoginManagerTd
    private lateinit var mUserEndpointTd: UserEndpointTd
    private lateinit var mEpisodesCacheTd: EpisodesCacheTd

    @Mock
    lateinit var mListenerMock1: ManagerViewModel.Listener
    @Mock
    lateinit var mListenerMock2: ManagerViewModel.Listener
    // endregion helper fields

    lateinit var SUT: ManagerViewModel

    @Before
    fun setup() {
        mLoginManagerTd = LoginManagerTd()
        mUserEndpointTd = UserEndpointTd()
        mEpisodesCacheTd = EpisodesCacheTd()
        SUT = ManagerViewModel(
            mLoginManagerTd,
            mUserEndpointTd,
            mEpisodesCacheTd
        )

        mLoginManagerTd.mGoogleSignInAccount = GOOGLE_SIGN_IN_ACCOUNT_MOCK
        isFirstSetup()
        silentSignInSuccess()
        signOutSuccess()
        setIsFirstSetUpSuccess()
        clearDatabaseSuccess()
        clearLastPlayedEpisodeSuccess()
        getSignedIAccountSuccess()
        getMatchingUserSuccess()
    }

    @Test
    fun getIsFirstSetupAndNotify_isFirstSetup_listenersNotifiedWithCorrectValue() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getIsFirstSetupAndNotify()
        // Assert
        verify(mListenerMock1).onFirstSetup()
        verify(mListenerMock2).onFirstSetup()
    }

    @Test
    fun getIsFirstSetupAndNotify_isSubsequentSetup_listenersNotifiedWithCorrectValue() {
        // Assert
        isSubsequentSetup()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getIsFirstSetupAndNotify()
        // Assert
        verify(mListenerMock1).onSubsequentSetup()
        verify(mListenerMock2).onSubsequentSetup()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun silentSignInAndNotify_notifyProcessing() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.silentSignInAndNotify()
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun silentSignInAndNotify_currentUserIsNullSilentSignInSuccess_listenersNotifiedOfSuccess() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.silentSignInAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(1))
        verify(mListenerMock1).onSilentSignInSuccess()
        verify(mListenerMock2).onSilentSignInSuccess()
    }

    @Test
    fun silentSignInAndNotify_currentUserIsNotNullSilentSignInSuccess_loginManagerNotAccessedListenersNotNotified() {
        // Assert
        SUT.currentUser = UserMocks.GET_USER()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.silentSignInAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(0))
        verify(mListenerMock1, never()).onSilentSignInSuccess()
        verify(mListenerMock2, never()).onSilentSignInSuccess()
        verify(mListenerMock1, never()).onSilentSignInFailed(
            TestUtils.kotlinAny(Intent::class.java), ArgumentMatchers.anyInt()
        )
        verify(mListenerMock2, never()).onSilentSignInFailed(
            TestUtils.kotlinAny(Intent::class.java), ArgumentMatchers.anyInt()
        )
    }

    @Test
    fun silentSignInAndNotify_currentUserIsNullSilentSignInSuccess_unregisteredListenersNotNotified() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.silentSignInAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(1))
        verify(mListenerMock1).onSilentSignInSuccess()
        verify(mListenerMock2, never()).onSilentSignInSuccess()
    }

    @Test
    fun silentSignInAndNotify_currentUserIsNullSilentSignInError_listenersNotifiedOfError() {
        // Assert
        silentSignInError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.silentSignInAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSilentSignInCounter, `is`(1))
        verify(mListenerMock1).onSilentSignInFailed(
            TestUtils.kotlinAny(Intent::class.java), ArgumentMatchers.anyInt()
        )
        verify(mListenerMock2).onSilentSignInFailed(
            TestUtils.kotlinAny(Intent::class.java), ArgumentMatchers.anyInt()
        )
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun signOutAndNotify_notifyProcessing() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.signOutAndNotify()
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun signOutAndNotify_signOutSuccess_userVariableNullified() {
        // Assert
        SUT.currentUser = UserMocks.GET_USER()
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertNull(SUT.currentUser)
    }

    @Test
    fun signOutAndNotify_signOutSuccess_correctValueSentToSetIsFirstSetup() {
        // Assert
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetIsFirstSetupCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetIsFirstSetupArgIsFirstSetup, `is`(true))
    }

    @Test
    fun signOutAndNotify_signOutSuccess_isFirstSetupCalled() {
        // Assert
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsFirstSetupCounter, `is`(1))
    }

    @Test
    fun signOutAndNotify_signOutSuccessSetIsFirstSetupSuccess_clearDatabasePolled() {
        // Assert
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mClearDatabaseCounter, `is`(1))
    }

    @Test
    fun signOutAndNotify_signOutSuccessSetIsFirstSetupSuccessClearDatabaseSuccess_clearLastPlayedEpisodePolled() {
        // Assert
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertThat(mEpisodesCacheTd.mClearLastPlayedEpisodeCounter, `is`(1))
    }

    @Test
    fun signOutAndNotify_signOutSuccessSetIsFirstSetupSuccessClearDatabaseSuccessClearLastPlayedEpisodeSuccess_falseValueSentToSetSubscriptionsSynced() {
        // Assert
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetSubscriptionsSyncedArgIsSubscriptionSynced, `is`(false))
    }

    @Test
    fun signOutAndNotify_signOutError_listenersNotifiedOfError() {
        // Assert
        signOutError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.signOutAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mSignOutCounter, `is`(1))
        verify(mListenerMock1).onSignOutFailed()
        verify(mListenerMock2).onSignOutFailed()
    }

    @Test
    fun signOutAndNotify_signOutSuccessSetIsFirstSetupError_listenersNotifiedOfError() {
        // Assert
        isSubsequentSetup()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.signOutAndNotify()
        // Assert
        verify(mListenerMock1).onSignOutFailed()
        verify(mListenerMock2).onSignOutFailed()
    }

    @Test
    fun signOutAndNotify_signOutSuccessSetIsFirstSetupSuccessClearDatabaseError_listenersNotifiedOfError() {
        // Assert
        clearDatabaseError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.signOutAndNotify()
        // Assert
        verify(mListenerMock1).onSignOutFailed()
        verify(mListenerMock2).onSignOutFailed()
    }

    @Test
    fun signOutAndNotify_signOutSuccessSetIsFirstSetupSuccessClearDatabaseSuccessClearLastPlayedEpisodeError_listenersNotifiedOfError() {
        // Assert
        clearLastPlayedEpisodeError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.signOutAndNotify()
        // Assert
        verify(mListenerMock1).onSignOutFailed()
        verify(mListenerMock2).onSignOutFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun onRequestResultReceived_requestCodeNotSignIn_loginManagerNotAccessedListenersNotNotified() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onRequestResultReceived(
            REQUEST_CODE_NOT_SIGN_IN, Activity.RESULT_OK,
            INTENT_MOCK
        )
        // Assert
        assertThat(mLoginManagerTd.mGetSignedInAccountCounter, `is`(0))
        verify(mListenerMock1, never()).onSignInSuccess()
        verify(mListenerMock2, never()).onSignInSuccess()
        verify(mListenerMock1, never()).onSignInFailed()
        verify(mListenerMock2, never()).onSignInFailed()
    }

    @Test
    fun onRequestResultReceived_requestCodeSignInResultCodeOkGetSignedInAccountSuccess_listenersNotifiedOfSuccess() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onRequestResultReceived(
            ManagerViewModel.REQUEST_CODE_SIGN_IN,
            Activity.RESULT_OK,
            INTENT_MOCK
        )
        // Assert
        assertThat(mLoginManagerTd.mGetSignedInAccountCounter, `is`(1))
        verify(mListenerMock1).onSignInSuccess()
        verify(mListenerMock2).onSignInSuccess()
    }

    @Test
    fun onRequestResultReceived_requestCodeSignInResultCodeOkGetSignedInAccountSuccess_unsubscribedListenersNotNotified() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.onRequestResultReceived(
            ManagerViewModel.REQUEST_CODE_SIGN_IN,
            Activity.RESULT_OK,
            INTENT_MOCK
        )
        // Assert
        assertThat(mLoginManagerTd.mGetSignedInAccountCounter, `is`(1))
        verify(mListenerMock1).onSignInSuccess()
        verify(mListenerMock2, never()).onSignInSuccess()
    }

    @Test
    fun onRequestResultReceived_requestCodeSignInResultCodeOkGetSignedInAccountFailure_listenersNotifiedOfError() {
        // Assert
        getSignedIAccountFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onRequestResultReceived(
            ManagerViewModel.REQUEST_CODE_SIGN_IN,
            Activity.RESULT_OK,
            INTENT_MOCK
        )
        // Assert
        assertThat(mLoginManagerTd.mGetSignedInAccountCounter, `is`(1))
        verify(mListenerMock1).onSignInFailed()
        verify(mListenerMock2).onSignInFailed()
    }

    @Test
    fun onRequestResultReceived_requestCodeSignInResultCodeNotOk_listenersNotifiedOfError() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onRequestResultReceived(
            ManagerViewModel.REQUEST_CODE_SIGN_IN,
            Activity.RESULT_CANCELED,
            INTENT_MOCK
        )
        // Assert
        assertThat(mLoginManagerTd.mGetSignedInAccountCounter, `is`(0))
        verify(mListenerMock1).onSignInFailed()
        verify(mListenerMock2).onSignInFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun getMatchingUserAndNotify_notifyProcessing() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getMatchingUserAndNotify()
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun getMatchingUserAndNotify_getMatchingUserSuccess_receivedUserSavedToVariable() {
        // Assert
        SUT.currentUser = null
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getMatchingUserAndNotify()
        // Assert
        assertThat(mUserEndpointTd.mGetMatchingUserCounter, `is`(1))
        assertThat(SUT.currentUser, `is`(UserMocks.GET_USER()))
    }

    @Test
    fun getMatchingUserAndNotify_getMatchingUserSuccess_listenersNotifiedOfSuccess() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getMatchingUserAndNotify()
        // Assert
        assertThat(mUserEndpointTd.mGetMatchingUserCounter, `is`(1))
        verify(mListenerMock1).onUserFetchSuccess()
        verify(mListenerMock2).onUserFetchSuccess()
    }

    @Test
    fun getMatchingUserAndNotify_getMatchingUserSuccess_getMatchingUserNotCalledTwice() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getMatchingUserAndNotify()
        SUT.getMatchingUserAndNotify()
        // Assert
        assertThat(mUserEndpointTd.mGetMatchingUserCounter, `is`(1))
        verify(mListenerMock1, times(1)).notifyProcessing()
        verify(mListenerMock2, times(1)).notifyProcessing()
    }

    @Test
    fun getMatchingUserAndNotify_getMatchingUserSuccess_unregisteredListenersNotNotifiedOfSuccess() {
        // Assert
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.getMatchingUserAndNotify()
        // Assert
        assertThat(mUserEndpointTd.mGetMatchingUserCounter, `is`(1))
        verify(mListenerMock1).onUserFetchSuccess()
        verify(mListenerMock2, never()).onUserFetchSuccess()
    }

    @Test
    fun getMatchingUserAndNotify_getMatchingUserError_listenersNotifiedOfError() {
        // Assert
        getMatchingUserError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getMatchingUserAndNotify()
        // Assert
        assertThat(mUserEndpointTd.mGetMatchingUserCounter, `is`(1))
        verify(mListenerMock1).onUserFetchFailed()
        verify(mListenerMock2).onUserFetchFailed()
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun getIsFirstSetupAndNotify_isFirstSetupCalled() {
        // Assert
        // Act
        SUT.getIsFirstSetupAndNotify()
        // Assert
        assertThat(mLoginManagerTd.mIsFirstSetupCounter, `is`(1))
    }

    //----------------------------------------------------------------------------------------------

    @Test
    fun setIsFirstSetUp_correctBooleanPassedAsArgument() {
        // Assert
        // Act
        SUT.setIsFirstSetUp(true)
        // Assert
        assertThat(mLoginManagerTd.mSetIsFirstSetupCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetIsFirstSetupArgIsFirstSetup, `is`(true))
    }

    @Test
    fun setIsFirstSetUp_isFirstSetUpCalled() {
        // Assert
        // Act
        SUT.setIsFirstSetUp(false)
        // Assert
        assertThat(mLoginManagerTd.mSetIsFirstSetupCounter, `is`(1))
        assertThat(mLoginManagerTd.mSetIsFirstSetupArgIsFirstSetup, `is`(false))
    }

    // region helper methods
    fun isFirstSetup() {
        // no-op because mIsFirstSetupStateIsFirstSetUp false by default
    }

    fun isSubsequentSetup() {
        mLoginManagerTd.mIsFirstSetupStateIsFirstSetUp = false
    }

    fun silentSignInSuccess() {
        // no-op because mSilentSignInFailure false by default
    }

    fun silentSignInError() {
        mLoginManagerTd.mSilentSignInFailure = true
    }

    fun signOutSuccess() {
        // no-op because mSignOutFailure false by default
    }

    fun signOutError() {
        mLoginManagerTd.mSignOutFailure = true
    }

    fun setIsFirstSetUpSuccess() {
        // no-op because mClearDatabaseFailure false by default
    }

    fun clearDatabaseSuccess() {
        // no-op because mClearDatabaseFailure false by default
    }

    fun clearDatabaseError() {
        mLoginManagerTd.mClearDatabaseFailure = true
    }

    fun clearLastPlayedEpisodeSuccess() {
        // no-op because mClearDatabaseFailure false by default
    }

    fun clearLastPlayedEpisodeError() {
        mEpisodesCacheTd.mClearLastPlayedEpisodeError = true
    }

    fun getSignedIAccountSuccess() {
        // no-op because mGetSignedInAccountFailure false by default
    }

    fun getSignedIAccountFailure() {
        mLoginManagerTd.mGetSignedInAccountFailure = true
    }

    fun getMatchingUserSuccess() {
        // no-op because mGetMatchingUserFailure false by default
    }

    fun getMatchingUserError() {
        mUserEndpointTd.mGetMatchingUserFailure = true
    }
    // endregion helper methods

    // region helper classes
    class UserEndpointTd : UserEndpoint(null, null) {

        var mGetMatchingUserCounter = 0
        var mGetMatchingUserFailure = false
        override fun getMatchingUser(listener: GetUserListener) {
            mGetMatchingUserCounter += 1
            if (!mGetMatchingUserFailure) {
                listener.onUserFetchSuccess(UserMocks.GET_USER())
            } else {
                listener.onUserFetchFailed()
            }
        }
    }
    // endregion helper classes

}