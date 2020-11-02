package com.atmko.skiptoit.search.searchchild

import android.os.Bundle
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.testdata.PodcastDataSourceProviderMapMocks
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SearchViewModelTest {

    // region constants

    // endregion constants

    // end region helper fields
    lateinit var dataSourceFactoryTd: PodcastDataSourceFactoryTd

    @Mock lateinit var mListenerMock1: SearchViewModel.Listener
    @Mock lateinit var mListenerMock2: SearchViewModel.Listener

    @Mock lateinit var mBundleMock: Bundle
    // endregion helper fields

    lateinit var SUT: SearchViewModel

    @Before
    fun setup() {
        dataSourceFactoryTd = PodcastDataSourceFactoryTd()
        SUT = SearchViewModel(dataSourceFactoryTd)

        scrollPositionSavedInBundle()
    }

    @Test
    fun handleSavedState_nullBundle_notifyScrollStateRestored() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(null)
        // Assert
        verify(mListenerMock1).onScrollPositionRestored(0)
        verify(mListenerMock2).onScrollPositionRestored(0)
    }

    @Test
    fun handleSavedState_notBullBundleScrollPositionSavedInBundle_notifyScrollStateRestored() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        verify(mListenerMock1).onScrollPositionRestored(1)
        verify(mListenerMock2).onScrollPositionRestored(1)
    }

    // region helper methods
    private fun scrollPositionSavedInBundle() {
        Mockito.`when`(mBundleMock.getInt(SearchViewModel.SCROLL_POSITION_KEY, 0)).thenReturn(1)
    }
    // endregion helper methods

    // region helper classes
    class PodcastDataSourceFactoryTd : PodcastDataSourceFactory(PodcastDataSourceProviderMapMocks.GET_PROVIDER_MAP()) {

    }
    // endregion helper classes

}