package com.atmko.skiptoit.createcomment

import com.atmko.skiptoit.testclass.CommentCacheTd
import com.atmko.skiptoit.testdata.CommentMocks
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
class CreateCommentViewModelTest {

    // region constants
    val PODCAST_ID: String = "podcastId"
    val EPISODE_ID: String = "episodeId"
    val COMMENT_BODY: String = "commentBody"
    // endregion constants

    // end region helper fields
    private lateinit var mCreateCommentEndpointTd: CreateCommentEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    @Mock
    lateinit var mListenerMock1: CreateCommentViewModel.Listener

    @Mock
    lateinit var mListenerMock2: CreateCommentViewModel.Listener
    // endregion helper fields

    private lateinit var SUT: CreateCommentViewModel

    @Before
    fun setup() {
        mCreateCommentEndpointTd =
            CreateCommentEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = CreateCommentViewModel(mCreateCommentEndpointTd, mCommentCacheTd)
    }

    @Test
    fun createCommentAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun createCommentAndNotify_correctPodcastIdEpisodeIdAndCommentBodyPassedToEndPoint() {
        // Arrange
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        assertThat(mCreateCommentEndpointTd.mCreateCommentCounter, `is`(1))
        assertThat(mCreateCommentEndpointTd.mPodcastId, `is`(PODCAST_ID))
        assertThat(mCreateCommentEndpointTd.mEpisodeId, `is`(EPISODE_ID))
        assertThat(mCreateCommentEndpointTd.mCommentBody, `is`(COMMENT_BODY))
    }

    @Test
    fun createCommentAndNotify_endpointSuccess_correctEpisodeIdPassedToGetLastCommentPageTracker() {
        // Arrange
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        assertThat(mCommentCacheTd.mGetLastCommentPageTrackerCounter, `is`(1))
        assertThat(mCommentCacheTd.mGetLastCommentPageTrackerArgEpisodeId, `is`(EPISODE_ID))
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNullTrackerReturned_listenerNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated()
        verify(mListenerMock2).onCommentCreated()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNonNullTrackerReturnedNextPage_listenerNotifiedOfSuccess() {
        // Arrange
        getLastCommentPageTrackerTrackerReturned()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated()
        verify(mListenerMock2).onCommentCreated()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNonNullTrackerReturnedNoNextPage_correctEpisodeIdPassedToDeleteCommentPage() {
        // Arrange
        getLastCommentPageTrackerTrackerReturned()
        getLastCommentPageTrackerNoNextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        assertThat(mCommentCacheTd.mDeleteAllCommentsInPageCounter, `is`(1))
        assertThat(mCommentCacheTd.mDeleteAllCommentsInPageArgEpisodeId, `is`(EPISODE_ID))
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeletePageSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        getLastCommentPageTrackerTrackerReturned()
        getLastCommentPageTrackerNoNextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated()
        verify(mListenerMock2).onCommentCreated()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeletePageSuccess_unsubscribedObserversNotNotified() {
        //Arrange
        getLastCommentPageTrackerTrackerReturned()
        getLastCommentPageTrackerNoNextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated()
        verify(mListenerMock2, never()).onCommentCreated()
    }

    @Test
    fun createCommentAndNotify_endpointError_listenersNotifiedOfError() {
        // Arrange
        endpointFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreateFailed()
        verify(mListenerMock2).onCommentCreateFailed()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerError_listenersNotifiedOfError() {
        // Arrange
        getLastCommentPageTrackerError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onPageTrackerFetchFailed()
        verify(mListenerMock2).onPageTrackerFetchFailed()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerError_listenerNotifiedOfCommentCreation() {
        // Arrange
        getLastCommentPageTrackerError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated()
        verify(mListenerMock2).onCommentCreated()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeleteError_listenersNotifiedOfError() {
        // Arrange
        getLastCommentPageTrackerTrackerReturned()
        getLastCommentPageTrackerNoNextPage()
        deletePageError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentPageDeleteFailed()
        verify(mListenerMock2).onCommentPageDeleteFailed()
    }

    @Test
    fun createCommentAndNotify_endpointSuccessGetLastCommentPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeletePageError_listenersNotifiedOfSuccess() {
        // Arrange
        getLastCommentPageTrackerTrackerReturned()
        getLastCommentPageTrackerNoNextPage()
        deletePageError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated()
        verify(mListenerMock2).onCommentCreated()
    }

    // region helper methods
    private fun endpointFailure() {
        mCreateCommentEndpointTd.mFailure = true
    }

    private fun getLastCommentPageTrackerTrackerReturned() {
        mCommentCacheTd.mGetLastCommentPageTrackerNullPageTracker = false
    }

    private fun getLastCommentPageTrackerNoNextPage() {
        mCommentCacheTd.mGetLastCommentPageTrackerNoNextPage = true
    }

    private fun getLastCommentPageTrackerError() {
        mCommentCacheTd.mGetLastCommentPageTrackerFailure = true
    }

    private fun deletePageError() {
        mCommentCacheTd.mDeleteAllCommentsInPageFailure = true
    }
    // endregion helper methods

    // region helper classes
    class CreateCommentEndpointTd : CreateCommentEndpoint(null, null) {

        var mCreateCommentCounter = 0
        var mPodcastId: String = ""
        var mEpisodeId: String = ""
        var mCommentBody: String = ""
        var mFailure = false

        override fun createComment(
            podcastId: String,
            episodeId: String,
            commentBody: String,
            listener: Listener
        ) {
            mCreateCommentCounter++
            mPodcastId = podcastId
            mEpisodeId = episodeId
            mCommentBody = commentBody
            if (!mFailure) {
                listener.onCreateSuccess(CommentMocks.GET_COMMENT_1())
            } else {
                listener.onCreateFailed()
            }
        }
    }
    // endregion helper classes
}