package com.atmko.skiptoit.episodelist

import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.testclass.EpisodesCacheTd
import com.atmko.skiptoit.testdata.EpisodeMocks
import com.atmko.skiptoit.testdata.PodcastMocks
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
class EpisodeBoundaryCallbackTest {

    // region constants
    companion object {
        const val PODCAST_ID = "podcastId"
        val PAGING_KEY_START: Long? = null
    }

    // endregion constants

    // end region helper fields
    lateinit var mGetEpisodesEndpointTd: GetEpisodesEndpointTd
    lateinit var mEpisodesCacheTd: EpisodesCacheTd

    @Mock
    lateinit var mListenerMock1: BaseBoundaryCallback.Listener
    @Mock
    lateinit var mListenerMock2: BaseBoundaryCallback.Listener
    // endregion helper fields

    lateinit var SUT: EpisodeBoundaryCallback

    @Before
    fun setup() {
        mGetEpisodesEndpointTd = GetEpisodesEndpointTd()
        mEpisodesCacheTd = EpisodesCacheTd()
        SUT = EpisodeBoundaryCallback(mGetEpisodesEndpointTd, mEpisodesCacheTd)

        SUT.param = PODCAST_ID
        endpointSuccess()
        previousEpisodesAvailable()
        cacheSuccess()
    }

    @Test
    fun onZeroItemsLoaded_notifyPageLoading() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        verify(mListenerMock1).onPageLoading()
        verify(mListenerMock2).onPageLoading()
    }

    @Test
    fun onZeroItemsLoaded_correctParentIdAndLoadKeyPassedToEndpoint() {
        // Arrange
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        assertThat(mGetEpisodesEndpointTd.mGetEpisodesArgPodcastId, `is`(PODCAST_ID))
        assertThat(mGetEpisodesEndpointTd.mGetEpisodesArgPublishedAfterDate, `is`(PAGING_KEY_START))
    }

    @Test
    fun onZeroItemsLoaded_correctPodcastDetailsLoadTypeAndPodcastIdPassedToEpisodesCache() {
        // Arrange
        prevEpisodesUnavailable()
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        assertThat(mEpisodesCacheTd.mPodcastDetails, `is`(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES()))
        assertThat(mEpisodesCacheTd.mLoadType, `is`(BaseBoundaryCallback.loadTypeRefresh))
        assertThat(mEpisodesCacheTd.mParam, `is`(PODCAST_ID))
    }

    @Test
    fun onZeroItemsLoaded_endPointSuccessCacheSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2).onPageLoad()
    }

    @Test
    fun onZeroItemsLoaded_endPointSuccessCacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2, never()).onPageLoad()
    }

    @Test
    fun onZeroItemsLoaded_endPointSuccessCacheError_listenersNotifiedOfError() {
        // Arrange
        cacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    @Test
    fun onZeroItemsLoaded_endpointError_listenersNotifiedOfError() {
        // Arrange
        endpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    //---------------
    @Test
    fun onItemAtEndLoaded_notifyPageLoading() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(EpisodeMocks.GET_EPISODE_1())
        // Assert
        verify(mListenerMock1).onPageLoading()
        verify(mListenerMock2).onPageLoading()
    }

    @Test
    fun onItemAtEndLoaded_endPointSuccess_cacheSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(EpisodeMocks.GET_EPISODE_1())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2).onPageLoad()
    }

    @Test
    fun onItemAtEndLoaded_endPointSuccess_cacheSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(EpisodeMocks.GET_EPISODE_1())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2, never()).onPageLoad()
    }

    @Test
    fun onItemAtEndLoaded_endPointSuccessCacheError_listenersNotifiedOfError() {
        // Arrange
        cacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(EpisodeMocks.GET_EPISODE_1())
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    @Test
    fun onItemAtEndLoaded_endpointError_listenersNotifiedOfError() {
        // Arrange
        endpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(EpisodeMocks.GET_EPISODE_1())
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    // onItemAtFrontLoaded() not in use

    // region helper methods
    private fun endpointSuccess() {
        // no-op because mFailure false by default
    }

    private fun endpointError() {
        mGetEpisodesEndpointTd.mGetEpisodesError = true
    }

    private fun previousEpisodesAvailable() {
        // no-op because mNoPreviousEpisodes false by default
    }

    private fun prevEpisodesUnavailable() {
        mGetEpisodesEndpointTd.mNoPreviousEpisodes = true
    }

    private fun cacheSuccess() {
        // no-op because mFailure false by default
    }

    private fun cacheError() {
        mEpisodesCacheTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    class GetEpisodesEndpointTd : GetEpisodesEndpoint(null) {

        var mGetEpisodesCounter = 0
        var mGetEpisodesError = false
        var mNoPreviousEpisodes = false
        lateinit var mGetEpisodesArgPodcastId: String
        var mGetEpisodesArgPublishedAfterDate: Long? = null
        override fun getEpisodes(
            podcastId: String,
            publishedAfterDate: Long?,
            listener: Listener
        ) {
            mGetEpisodesCounter += 1
            mGetEpisodesArgPodcastId = podcastId
            mGetEpisodesArgPublishedAfterDate = publishedAfterDate
            if (!mGetEpisodesError) {
                if (!mNoPreviousEpisodes) {
                    listener.onEpisodesQuerySuccess(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITH_EPISODES())
                } else {
                    listener.onEpisodesQuerySuccess(PodcastMocks.PodcastDetailsMocks.GET_PODCAST_DETAILS_WITHOUT_EPISODES())
                }
            } else {
                listener.onEpisodesQueryFailed()
            }
        }
    }

    // endregion helper classes
}