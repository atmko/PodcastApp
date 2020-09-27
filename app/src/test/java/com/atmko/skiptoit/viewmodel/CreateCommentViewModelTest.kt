package com.atmko.skiptoit.viewmodel

import com.atmko.skiptoit.createcomment.CommentPageTrackerHelper
import com.atmko.skiptoit.createcomment.CreateCommentEndpoint
import com.atmko.skiptoit.createcomment.CreateCommentViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.testdata.CommentMocks
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
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
    lateinit var mCommentPageTrackerHelperTd: CommentPageTrackerHelperTd

    @Mock
    lateinit var mListenerMock1: CreateCommentViewModel.Listener
    @Mock
    lateinit var mListenerMock2: CreateCommentViewModel.Listener
    // endregion helper fields

    private lateinit var SUT: CreateCommentViewModel

    @Before
    fun setup() {
        mCreateCommentEndpointTd = CreateCommentEndpointTd()
        mCommentPageTrackerHelperTd = CommentPageTrackerHelperTd()
        SUT = CreateCommentViewModel(mCreateCommentEndpointTd, mCommentPageTrackerHelperTd)
        success()
    }

    @Test
    fun createCommentAndNotify_correctPodcastIdEpisodeIdAndCommentBodyPassedToEndPoint() {
        // Arrange
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        assertThat(mCreateCommentEndpointTd.mPodcastId, `is`(PODCAST_ID))
        assertThat(mCreateCommentEndpointTd.mEpisodeId, `is`(EPISODE_ID))
        assertThat(mCreateCommentEndpointTd.mCommentBody, `is`(COMMENT_BODY))
    }

    @Test
    fun createCommentAndNotify_success_listenersNotifiedOfProcessing() {
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
    fun createCommentAndNotify_correctCommentPassedToCommentPageTrackerHelper() {
        // Arrange
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        assertThat(mCommentPageTrackerHelperTd.mComment, `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun createCommentAndNotify_success_listenersNotifiedWithCorrectComment() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Comment> = ArgumentCaptor.forClass(Comment::class.java)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated(ac.capture())
        verify(mListenerMock2).onCommentCreated(ac.capture())
        assertThat(ac.allValues[0], `is`(CommentMocks.GET_COMMENT_1()))
        assertThat(ac.allValues[1], `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun createCommentAndNotify_success_unsubscribedObserversNotNotified() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreated(ArgumentMatchers.any())
        verify(mListenerMock2, never()).onCommentCreated(ArgumentMatchers.any())
    }

    @Test
    fun createCommentAndNotify_success_observersNotifiedOfFailure() {
        //Arrange
        failure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createCommentAndNotify(PODCAST_ID, EPISODE_ID, COMMENT_BODY)
        // Assert
        verify(mListenerMock1).onCommentCreateFailed()
        verify(mListenerMock2).onCommentCreateFailed()
    }

    // region helper methods
    private fun success() {
        // no-op because mFailure false by default
    }

    // region helper methods
    private fun failure() {
        mCreateCommentEndpointTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    class CreateCommentEndpointTd : CreateCommentEndpoint(null, null) {

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

    class CommentPageTrackerHelperTd : CommentPageTrackerHelper(null, null) {

        var mComment: Comment? = null
        var mFailure = false

        override fun updatePagingTracker(comment: Comment, listener: Listener) {
            mComment = comment
            if (!mFailure) {
                listener.onPagingDataUpdated(comment)
            }
        }
    }
    // endregion helper classes
}