package com.atmko.skiptoit.search.searchparent

import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.testdata.PodcastDataSourceProviderMapMocks.Companion.GET_PROVIDER_MAP
import com.atmko.skiptoit.testutils.TestUtils
import com.atmko.skiptoit.testutils.kotlinCapture
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SearchParentViewModelTest {

    // region constants
    companion object {
        const val QUERY_STRING = "queryString"
    }

    // endregion constants

    // end region helper fields
    lateinit var dataSourceFactoryTd: PodcastDataSourceFactoryTd

    @Mock lateinit var mListenerMock1: SearchParentViewModel.Listener
    @Mock lateinit var mListenerMock2: SearchParentViewModel.Listener
    // endregion helper fields

    lateinit var SUT: SearchParentViewModel

    @Before
    fun setup() {
        dataSourceFactoryTd = PodcastDataSourceFactoryTd()
        SUT = SearchParentViewModel(dataSourceFactoryTd)
    }

    @Test
    fun toggleSearchModeAndNotify_searchModeGenre_searchModeManualSavedToVariable() {
        // Arrange
        SUT.searchMode = SearchParentViewModel.SEARCH_MODE_GENRE
        // Act
        SUT.activateManualModeAndNotify(QUERY_STRING)
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_MANUAL))
    }

    @Test
    fun toggleSearchModeAndNotify_searchModeGenre_listenersNotifiedWithCorrectValue() {
        // Arrange
        SUT.searchMode = SearchParentViewModel.SEARCH_MODE_GENRE
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        // Act
        SUT.activateManualModeAndNotify(QUERY_STRING)
        // Assert
        verify(mListenerMock1).onSearchModeManualActivated(ac.kotlinCapture())
        verify(mListenerMock2).onSearchModeManualActivated(ac.kotlinCapture())
        val captures = ac.allValues
        assertThat(captures[0], `is`(QUERY_STRING))
        assertThat(captures[1], `is`(QUERY_STRING))
    }

    @Test
    fun toggleSearchModeAndNotify_searchModeManual_searchModeGenreSavedToVariable() {
        // Arrange
        SUT.searchMode = SearchParentViewModel.SEARCH_MODE_MANUAL
        // Act
        SUT.activateGenreModeAndNotify()
        // Assert
        assertThat(SUT.searchMode, `is`(SearchParentViewModel.SEARCH_MODE_GENRE))
    }

    @Test
    fun toggleSearchModeAndNotify_searchModeManual_listenersNotifiedOfChange() {
        // Arrange
        SUT.searchMode = SearchParentViewModel.SEARCH_MODE_MANUAL
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.activateGenreModeAndNotify()
        // Assert
        verify(mListenerMock1).onSearchModeGenreActivated()
        verify(mListenerMock2).onSearchModeGenreActivated()
    }

    @Test
    fun restoreSearchModeAndNotify_searchModeGenre_listenersNotifiedOfGenreSearchRestore() {
        // Arrange
        SUT.searchMode = SearchParentViewModel.SEARCH_MODE_GENRE
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.restoreSearchModeAndNotify()
        // Assert
        verify(mListenerMock1).onSearchModeGenreActivated()
        verify(mListenerMock2).onSearchModeGenreActivated()
        verify(mListenerMock1, never()).onSearchModeManualActivated(TestUtils.kotlinAny(String::class.java))
        verify(mListenerMock2, never()).onSearchModeManualActivated(TestUtils.kotlinAny(String::class.java))
        verify(mListenerMock1, never()).onSearchModeManualRestored()
        verify(mListenerMock2, never()).onSearchModeManualRestored()
    }

    @Test
    fun restoreSearchModeAndNotify_searchModeManual_listenersNotifiedOfManualSearchRestore() {
        // Arrange
        SUT.searchMode = SearchParentViewModel.SEARCH_MODE_MANUAL
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.restoreSearchModeAndNotify()
        // Assert
        verify(mListenerMock1).onSearchModeManualRestored()
        verify(mListenerMock2).onSearchModeManualRestored()
        verify(mListenerMock1, never()).onSearchModeManualActivated(TestUtils.kotlinAny(String::class.java))
        verify(mListenerMock2, never()).onSearchModeManualActivated(TestUtils.kotlinAny(String::class.java))
        verify(mListenerMock1, never()).onSearchModeGenreActivated()
        verify(mListenerMock2, never()).onSearchModeGenreActivated()
    }

    // region helper methods

    // endregion helper methods

    // region helper classes
    class PodcastDataSourceFactoryTd : PodcastDataSourceFactory(GET_PROVIDER_MAP()) {

    }
    // endregion helper classes

}