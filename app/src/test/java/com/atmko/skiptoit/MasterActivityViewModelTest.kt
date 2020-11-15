package com.atmko.skiptoit

import android.content.Intent
import android.os.Bundle
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.common.ManagerViewModelTest
import com.atmko.skiptoit.testclass.EpisodesCacheTd
import com.atmko.skiptoit.testclass.LoginManagerTd
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MasterActivityViewModelTest {

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
    private lateinit var mUserEndpointTd: ManagerViewModelTest.UserEndpointTd
    private lateinit var mEpisodesCacheTd: EpisodesCacheTd

    @Mock lateinit var mListenerMock1: ManagerViewModel.Listener
    @Mock lateinit var mListenerMock2: ManagerViewModel.Listener

    @Mock lateinit var mMasterListenerMock1: MasterActivityViewModel.MasterListener
    @Mock lateinit var mMasterListenerMock2: MasterActivityViewModel.MasterListener

    @Mock lateinit var mBundleMock: Bundle
    // endregion helper fields

    lateinit var SUT: MasterActivityViewModel

    @Before
    fun setup() {
        mLoginManagerTd = LoginManagerTd()
        mUserEndpointTd = ManagerViewModelTest.UserEndpointTd()
        mEpisodesCacheTd = EpisodesCacheTd()
        SUT = MasterActivityViewModel(
            mLoginManagerTd,
            mUserEndpointTd,
            mEpisodesCacheTd
        )

        mLoginManagerTd.mGoogleSignInAccount = GOOGLE_SIGN_IN_ACCOUNT_MOCK
    }

    @Test
    fun handleBottomSheetDrag_slideEffectLessThan1_variablesCorrectlyUpdated() {
        // Arrange
        // Act
        SUT.handleBottomSheetDrag(0.3f)
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(false))
        assertThat(SUT.isBottomSheetShown, `is`(true))
    }

    @Test
    fun handleBottomSheetDrag_slideEffectEqualTo1_variablesCorrectlyUpdated() {
        // Arrange
        // Act
        SUT.handleBottomSheetDrag(1f)
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(true))
        assertThat(SUT.isBottomSheetShown, `is`(true))
    }
    // ---------------------------------------------------------------------------------------------

    @Test
    fun handleSavedStateAndNotify_nullBundle_notifyHideBottomSheet() {
        // Assert
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.handleSavedStateAndNotify(null)
        // Assert
        assertThat(SUT.isBottomSheetShown, `is`(false))
        verify(mMasterListenerMock1).onHideBottomSheet()
        verify(mMasterListenerMock2).onHideBottomSheet()
    }

    @Test
    fun handleSavedStateAndNotify_nullBundle_notifyCollapseBottomSheet() {
        // Assert
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.handleSavedStateAndNotify(null)
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(false))
        verify(mMasterListenerMock1).onCollapseBottomSheet()
        verify(mMasterListenerMock2).onCollapseBottomSheet()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleBottomSheetNotVisibleInBundle_notifyHideBottomSheet() {
        // Assert
        bottomSheetNotVisibleInBundle()
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.handleSavedStateAndNotify(mBundleMock)
        // Assert
        assertThat(SUT.isBottomSheetShown, `is`(false))
        verify(mMasterListenerMock1).onHideBottomSheet()
        verify(mMasterListenerMock2).onHideBottomSheet()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleBottomSheetVisibleInBundle_notifyShowBottomSheet() {
        // Assert
        bottomSheetVisibleInBundle()
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.handleSavedStateAndNotify(mBundleMock)
        // Assert
        assertThat(SUT.isBottomSheetShown, `is`(true))
        verify(mMasterListenerMock1).onShowBottomSheet()
        verify(mMasterListenerMock2).onShowBottomSheet()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleBottomSheetCollapsedInBundle_notifyCollapseBottomSheet() {
        // Assert
        bottomSheetCollapsedInBundle()
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.handleSavedStateAndNotify(mBundleMock)
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(false))
        verify(mMasterListenerMock1).onCollapseBottomSheet()
        verify(mMasterListenerMock2).onCollapseBottomSheet()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleBottomSheetExpandedInBundle_notifyExpandBottomSheet() {
        // Assert
        bottomSheetExpandedInBundle()
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.handleSavedStateAndNotify(mBundleMock)
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(true))
        verify(mMasterListenerMock1).onExpandBottomSheet()
        verify(mMasterListenerMock2).onExpandBottomSheet()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun expandBottomSheetAndNotify_notifyExpandBottomSheet() {
        // Assert
        bottomSheetExpandedInBundle()
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.expandBottomSheetAndNotify()
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(true))
        assertThat(SUT.isBottomSheetShown, `is`(true))
        verify(mMasterListenerMock1).onExpandBottomSheet()
        verify(mMasterListenerMock2).onExpandBottomSheet()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun collapseBottomSheetAndNotify_notifyCollapseBottomSheet() {
        // Assert
        bottomSheetCollapsedInBundle()
        SUT.registerMasterListener(mMasterListenerMock1)
        SUT.registerMasterListener(mMasterListenerMock2)
        // Act
        SUT.collapseBottomSheetAndNotify()
        // Assert
        assertThat(SUT.isBottomSheetExpanded, `is`(false))
        assertThat(SUT.isBottomSheetShown, `is`(true))
        verify(mMasterListenerMock1).onCollapseBottomSheet()
        verify(mMasterListenerMock2).onCollapseBottomSheet()
    }

    // region helper methods
    fun bottomSheetNotVisibleInBundle() {
        `when`(mBundleMock.getBoolean(MasterActivityViewModel.IS_BOTTOM_SHEET_SHOWN_KEY)).thenReturn(false)
    }

    fun bottomSheetVisibleInBundle() {
        `when`(mBundleMock.getBoolean(MasterActivityViewModel.IS_BOTTOM_SHEET_SHOWN_KEY)).thenReturn(true)
    }

    fun bottomSheetCollapsedInBundle() {
        `when`(mBundleMock.getBoolean(MasterActivityViewModel.IS_BOTTOM_SHEET_EXPANDED_KEY)).thenReturn(false)
    }

    fun bottomSheetExpandedInBundle() {
        `when`(mBundleMock.getBoolean(MasterActivityViewModel.IS_BOTTOM_SHEET_EXPANDED_KEY)).thenReturn(true)
    }
    // endregion helper methods

    // region helper classes

    // endregion helper classes
}
