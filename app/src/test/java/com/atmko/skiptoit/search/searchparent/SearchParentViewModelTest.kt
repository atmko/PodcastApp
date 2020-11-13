package com.atmko.skiptoit.search.searchparent

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.testdata.PodcastDataSourceProviderMapMocks.Companion.GET_PROVIDER_MAP
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SearchParentViewModelTest {

    // region constants
    companion object {
        const val QUERY_STRING = "queryString"

        fun GET_SEARCH_RESULTS_LIVEDATA(dataSourceFactoryTd: PodcastDataSourceFactoryTd): LiveData<PagedList<Podcast>> {
            return LivePagedListBuilder<Int, Podcast>(
                dataSourceFactoryTd,
                PagedList.Config.Builder().build()
            ).build()
        }
    }

    // endregion constants

    // end region helper fields
    lateinit var dataSourceFactoryTd: PodcastDataSourceFactoryTd

    @Mock lateinit var mListenerMock1: SearchParentViewModel.Listener
    @Mock lateinit var mListenerMock2: SearchParentViewModel.Listener

    @Mock lateinit var mBundleMock: Bundle
    // endregion helper fields

    lateinit var SUT: SearchParentViewModel

    @Before
    fun setup() {
        dataSourceFactoryTd = PodcastDataSourceFactoryTd()
        SUT = SearchParentViewModel(dataSourceFactoryTd)

        queryStringInBundle()
        tabPositionInBundle()
        genreSearchModeInBundle()
        hideSearchBoxInBundle()
        hideKeyboardInBundle()
    }

    @Test
    fun handleSavedStateAndNotify_nullBundle_notifyQueryStringRestoredWithCorrectValue() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(null)
        // Assert
        assertThat(SUT.queryString, `is`(""))
        verify(mListenerMock1).onQueryStringRestored("")
        verify(mListenerMock2).onQueryStringRestored("")
    }

    @Test
    fun handleSavedStateAndNotify_nullBundle_notifyTabPositionRestoredWithCorrectValue() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(null)
        // Assert
        assertThat(SUT.tabPosition, `is`(0))
        verify(mListenerMock1).onTabPositionRestored(0)
        verify(mListenerMock2).onTabPositionRestored(0)
    }

    @Test
    fun handleSavedStateAndNotify_nullBundle_activateSearchModeGenre() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(null)
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_GENRE))
        verify(mListenerMock1).onShowGenreLayout()
        verify(mListenerMock1).onHideManualLayout()
        verify(mListenerMock2).onShowGenreLayout()
        verify(mListenerMock2).onHideManualLayout()
    }

    @Test
    fun handleSavedStateAndNotify_nullBundle_notifyHideSearchBox() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(null)
        // Assert
        assertThat(SUT.isSearchBoxVisible, `is`(false))
        verify(mListenerMock1).onHideManualSearchBar()
        verify(mListenerMock2).onHideManualSearchBar()
    }

    @Test
    fun handleSavedStateAndNotify_nullBundle_notifyHideKeyboard() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(null)
        // Assert
        assertThat(SUT.isKeyboardVisible, `is`(false))
        verify(mListenerMock1).onHideKeyboard()
        verify(mListenerMock2).onHideKeyboard()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleQueryStringInBundle_notifyQueryStringRestoredWithCorrectValue() {
        // Arrange
        queryStringInBundle()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertThat(SUT.queryString, `is`(QUERY_STRING))
        verify(mListenerMock1).onQueryStringRestored(QUERY_STRING)
        verify(mListenerMock2).onQueryStringRestored(QUERY_STRING)
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleTabPositionInBundle_notifyTabPositionRestoredWithCorrectValue() {
        // Arrange
        tabPositionInBundle()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertThat(SUT.tabPosition, `is`(1))
        verify(mListenerMock1).onTabPositionRestored(1)
        verify(mListenerMock2).onTabPositionRestored(1)
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleEmptyQueryStringManualSearchModeInBundle_listenersNotNotified() {
        // Arrange
        emptyQueryStringInBundle()
        manualSearchModeInBundle()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_MANUAL))
        verify(mListenerMock1, never()).onShowManualLayout()
        verify(mListenerMock1, never()).onHideGenreLayout()
        verify(mListenerMock2, never()).onShowManualLayout()
        verify(mListenerMock2, never()).onHideGenreLayout()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleNonEmptyQueryStringManualSearchModeInBundleNonNullSearchResults_searchNotRefreshed() {
        // Arrange
        manualSearchModeInBundle()
        SUT.searchResults = GET_SEARCH_RESULTS_LIVEDATA(dataSourceFactoryTd)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        val expectedResults = GET_SEARCH_RESULTS_LIVEDATA(dataSourceFactoryTd)
        assertThat(SUT.searchResults!!.value, `is`(expectedResults.value))
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleNonEmptyQueryStringManualSearchModeInBundleNullSearchResults_searchRefreshed() {
        // Arrange
        manualSearchModeInBundle()
        SUT.searchResults = null
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertNotNull(SUT.searchResults)
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleNonEmptyQueryStringManualSearchModeInBundle_activateSearchModeManual() {
        // Arrange
        manualSearchModeInBundle()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_MANUAL))
        verify(mListenerMock1).onShowManualLayout()
        verify(mListenerMock1).onHideGenreLayout()
        verify(mListenerMock2).onShowManualLayout()
        verify(mListenerMock2).onHideGenreLayout()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundleShowSearchBoxInBundle_notifyShowSearchBox() {
        // Arrange
        showSearchBoxInBundle()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertThat(SUT.isSearchBoxVisible, `is`(true))
        verify(mListenerMock1).onShowManualSearchBar()
        verify(mListenerMock2).onShowManualSearchBar()
    }

    @Test
    fun handleSavedStateAndNotify_notNullBundle_notifyShowKeyBoard() {
        // Arrange
        showKeyboardInInBundle()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.handleSavedState(mBundleMock)
        // Assert
        assertThat(SUT.isKeyboardVisible, `is`(true))
        verify(mListenerMock1).onShowKeyboard()
        verify(mListenerMock2).onShowKeyboard()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun activateGenreModeAndNotify_notNullBundle_activateSearchModeGenre() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.activateGenreModeAndNotify()
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_GENRE))
        verify(mListenerMock1).onShowGenreLayout()
        verify(mListenerMock1).onHideManualLayout()
        verify(mListenerMock2).onShowGenreLayout()
        verify(mListenerMock2).onHideManualLayout()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun activateManualModeAndNotify_emptyQueryString_activateSearchModeManual() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.activateManualModeAndNotify("")
        // Assert
        verify(mListenerMock1, never()).onShowManualLayout()
        verify(mListenerMock1, never()).onHideGenreLayout()
        verify(mListenerMock2, never()).onShowManualLayout()
        verify(mListenerMock2, never()).onHideGenreLayout()
    }

    @Test
    fun activateManualModeAndNotify_nonEmptyQueryString_activateSearchModeManual() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.activateManualModeAndNotify(QUERY_STRING)
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_MANUAL))
        verify(mListenerMock1).onShowManualLayout()
        verify(mListenerMock1).onHideGenreLayout()
        verify(mListenerMock2).onShowManualLayout()
        verify(mListenerMock2).onHideGenreLayout()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun searchButtonClickedAndNotify_searchBoxVisible_listenersNotified() {
        // Arrange
        SUT.isSearchBoxVisible = true
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.searchButtonClickedAndNotify()
        // Assert
        verify(mListenerMock1).onShowGenreLayout()
        verify(mListenerMock1).onHideManualLayout()
        verify(mListenerMock1).onHideKeyboard()
        verify(mListenerMock1).onHideManualSearchBar()

        verify(mListenerMock2).onShowGenreLayout()
        verify(mListenerMock2).onHideManualLayout()
        verify(mListenerMock2).onHideKeyboard()
        verify(mListenerMock2).onHideManualSearchBar()
    }

    @Test
    fun searchButtonClickedAndNotify_searchBoxVisible_searchResultsNullified() {
        // Arrange
        SUT.isSearchBoxVisible = true
        SUT.queryString = QUERY_STRING
        SUT.searchResults = MutableLiveData()
        // Act
        SUT.searchButtonClickedAndNotify()
        // Assert
        assertNull(SUT.searchResults)
    }

    @Test
    fun searchButtonClickedAndNotify_searchBoxNotVisible_listenersNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.searchButtonClickedAndNotify()
        // Assert
        verify(mListenerMock1).onShowManualSearchBar()
        verify(mListenerMock1).onShowKeyboard()

        verify(mListenerMock2).onShowManualSearchBar()
        verify(mListenerMock2).onShowKeyboard()
    }

    // ---------------------------------------------------------------------------------------------

    // region helper methods
    private fun emptyQueryStringInBundle() {
        `when`(mBundleMock.getString(SearchParentViewModel.QUERY_STRING_KEY, "")).thenReturn(
            "")
    }

    private fun queryStringInBundle() {
        `when`(mBundleMock.getString(SearchParentViewModel.QUERY_STRING_KEY, "")).thenReturn(
            QUERY_STRING)
    }

    private fun tabPositionInBundle() {
        `when`(mBundleMock.getInt(SearchParentViewModel.TAB_POSITION_KEY)).thenReturn(1)
    }

    private fun genreSearchModeInBundle() {
        `when`(mBundleMock.getInt(SearchParentViewModel.SEARCH_MODE_KEY)).thenReturn(SearchParentViewModel.SEARCH_MODE_GENRE)
    }

    private fun manualSearchModeInBundle() {
        `when`(mBundleMock.getInt(SearchParentViewModel.SEARCH_MODE_KEY)).thenReturn(SearchParentViewModel.SEARCH_MODE_MANUAL)
    }

    private fun showSearchBoxInBundle() {
        `when`(mBundleMock.getBoolean(SearchParentViewModel.IS_SEARCH_BOX_VISIBLE_KEY, false)).thenReturn(true)
    }

    private fun hideSearchBoxInBundle() {
        `when`(mBundleMock.getBoolean(SearchParentViewModel.IS_SEARCH_BOX_VISIBLE_KEY, false)).thenReturn(false)
    }

    private fun showKeyboardInInBundle() {
        `when`(mBundleMock.getBoolean(SearchParentViewModel.IS_KEYBOARD_VISIBLE_KEY)).thenReturn(true)
    }

    private fun hideKeyboardInBundle() {
        `when`(mBundleMock.getBoolean(SearchParentViewModel.IS_KEYBOARD_VISIBLE_KEY)).thenReturn(false)
    }
    // endregion helper methods

    // region helper classes
    class PodcastDataSourceFactoryTd : PodcastDataSourceFactory(GET_PROVIDER_MAP()) {

    }
    // endregion helper classes

}