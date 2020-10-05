package com.atmko.skiptoit.episode.replies

import com.atmko.skiptoit.testclass.CommentCacheTd
import com.atmko.skiptoit.testdata.CommentMocks
import com.atmko.skiptoit.testdata.CommentResultsMocks
import com.atmko.skiptoit.common.BaseBoundaryCallback
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
class ReplyCommentBoundaryCallbackTest {

    // region constants
    companion object {
        const val PARENT_ID = "parent_id"
        const val PAGING_KEY_1 = 1
        const val PAGING_KEY_2 = 2
    }

    // endregion constants

    // end region helper fields
    private lateinit var mGetRepliesEndpointTd: GetRepliesEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    @Mock
    lateinit var mListenerMock1: BaseBoundaryCallback.Listener
    @Mock
    lateinit var mListenerMock2: BaseBoundaryCallback.Listener
    // endregion helper fields

    private lateinit var SUT: ReplyCommentBoundaryCallback

    @Before
    fun setup() {
        mGetRepliesEndpointTd = GetRepliesEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = ReplyCommentBoundaryCallback(mGetRepliesEndpointTd, mCommentCacheTd)

        SUT.param = PARENT_ID
        endpointSuccess()
        cacheSuccess()
        noNextPage()
        noPrevPage()
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
        assertThat(mGetRepliesEndpointTd.mParam, `is`(PARENT_ID))
        assertThat(mGetRepliesEndpointTd.mLoadKey, `is`(PAGING_KEY_1))
    }

    @Test
    fun onZeroItemsLoaded_correctCommentResultsAndLoadTypePassedToCommentCache() {
        // Arrange
        // Act
        SUT.onZeroItemsLoaded()
        // Assert
        assertThat(mCommentCacheTd.mCommentResults.comments,
            `is`(CommentResultsMocks.GET_COMMENT_RESULTS().comments))
        assertThat(mCommentCacheTd.mCommentResults.hasNext,
            `is`(CommentResultsMocks.GET_COMMENT_RESULTS().hasNext))
        assertThat(mCommentCacheTd.mCommentResults.hasPrev,
            `is`(CommentResultsMocks.GET_COMMENT_RESULTS().hasPrev))
        assertThat(mCommentCacheTd.mCommentResults.page,
            `is`(CommentResultsMocks.GET_COMMENT_RESULTS().page))
        assertThat(mCommentCacheTd.mLoadType, `is`(BaseBoundaryCallback.loadTypeRefresh))
        assertThat(mCommentCacheTd.mParam, `is`(PARENT_ID))
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
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoading()
        verify(mListenerMock2).onPageLoading()
    }

    @Test
    fun onItemAtEndLoaded_correctCommentIdPassedToCommentCache() {
        // Arrange
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        assertThat(mCommentCacheTd.mCommentId, `is`(CommentMocks.COMMENT_ID_1))
    }

    @Test
    fun onItemAtEndLoaded_cacheSuccessNoNextPageEndPointSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2).onPageLoad()
    }

    @Test
    fun onItemAtEndLoaded_cacheSuccessNoNextPageEndPointSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2, never()).onPageLoad()
    }

    @Test
    fun onItemAtEndLoaded_cacheSuccessNextPageEndPointSuccess_correctParentIdAndLoadKeyPassedToEndpoint() {
        // Arrange
        nextPage()
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        assertThat(mGetRepliesEndpointTd.mParam, `is`(PARENT_ID))
        assertThat(mGetRepliesEndpointTd.mLoadKey, `is`(PAGING_KEY_2))
    }

    @Test
    fun onItemAtEndLoaded_cacheSuccessNextPageEndPointSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        nextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2).onPageLoad()
    }

    @Test
    fun onItemAtEndLoaded_cacheSuccessNextPageEndPointError_listenersNotifiedOfSuccess() {
        // Arrange
        nextPage()
        endpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    @Test
    fun onItemAtEndLoaded_cacheError_listenersNotifiedOfFailure() {
        // Arrange
        cacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtEndLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    //----------------

    @Test
    fun onItemAtFrontLoaded_notifyPageLoading() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoading()
        verify(mListenerMock2).onPageLoading()
    }

    @Test
    fun onItemAtFrontLoaded_correctCommentIdPassedToCommentCache() {
        // Arrange
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        assertThat(mCommentCacheTd.mCommentId, `is`(CommentMocks.COMMENT_ID_1))
    }

    @Test
    fun onItemAtFrontLoaded_cacheSuccessNoPrevPageEndPointSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2).onPageLoad()
    }

    @Test
    fun onItemAtFrontLoaded_cacheSuccessNoPrevPageEndPointSuccess_unsubscribedListenersNotNotified() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2, never()).onPageLoad()
    }

    @Test
    fun onItemAtFrontLoaded_cacheSuccessPrevPageEndPointSuccess_correctParentIdAndLoadKeyPassedToEndpoint() {
        // Arrange
        prevPage()
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        assertThat(mGetRepliesEndpointTd.mParam, `is`(PARENT_ID))
        assertThat(mGetRepliesEndpointTd.mLoadKey, `is`(PAGING_KEY_1))
    }

    @Test
    fun onItemAtFrontLoaded_cacheSuccessPrevPageEndPointSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        prevPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoad()
        verify(mListenerMock2).onPageLoad()
    }

    @Test
    fun onItemAtFrontLoaded_cacheSuccessPrevPageEndPointError_listenersNotifiedOfSuccess() {
        // Arrange
        prevPage()
        endpointError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    @Test
    fun onItemAtFrontLoaded_cacheError_listenersNotifiedOfFailure() {
        // Arrange
        cacheError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.onItemAtFrontLoaded(CommentMocks.GET_REPLY())
        // Assert
        verify(mListenerMock1).onPageLoadFailed()
        verify(mListenerMock2).onPageLoadFailed()
    }

    // region helper methods
    private fun endpointSuccess() {
        // no-op because mFailure false by default
    }

    private fun endpointError() {
        mGetRepliesEndpointTd.mFailure = true
    }

    private fun cacheSuccess() {
        // no-op because mFailure false by default
    }

    private fun cacheError() {
        mCommentCacheTd.mFailure = true
    }

    private fun noNextPage() {
        // no-op because mNextPage false by default
    }

    private fun nextPage() {
        mCommentCacheTd.mNextPage = true
    }

    private fun noPrevPage() {
        // no-op because mPrevPage false by default
    }

    private fun prevPage() {
        mCommentCacheTd.mPrevPage = true
    }
    // endregion helper methods

    // region helper classes
    class GetRepliesEndpointTd : GetRepliesEndpoint(null, null) {

        var mParam: String = ""
        var mLoadKey: Int? = null
        var mFailure = false

        override fun getReplies(param: String, loadKey: Int, listener: Listener) {
            mParam = param
            mLoadKey = loadKey

            if (!mFailure) {
                listener.onQuerySuccess(CommentResultsMocks.GET_COMMENT_RESULTS())
            } else {
                listener.onQueryFailed()
            }
        }
    }
    // endregion helper classes
}