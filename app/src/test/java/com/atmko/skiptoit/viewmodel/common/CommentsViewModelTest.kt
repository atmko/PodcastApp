package com.atmko.skiptoit.viewmodel.common

import com.atmko.skiptoit.episode.common.CommentsEndpoint
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.model.Comment
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
class CommentsViewModelTest {

    // region constants

    // endregion constants

    // end region helper fields
    private lateinit var mCommentsEndpointTd: CommentsEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    @Mock
    lateinit var mListenerMock1: CommentsViewModel.Listener
    @Mock
    lateinit var mListenerMock2: CommentsViewModel.Listener
    // endregion helper fields

    private lateinit var SUT: CommentsViewModel

    @Before
    fun setup() {
        mCommentsEndpointTd = CommentsEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = CommentsViewModel(
            mCommentsEndpointTd,
            mCommentCacheTd
        )
        voteSuccess()
        deleteSuccess()
    }

    @Test
    fun upVoteAndNotify_upVoteAndNotify_unvotedCommentCorrectCommentAndVoteWeightPassedToEndpoint() {
         // Arrange
         // Act
         SUT.upVoteAndNotify(CommentMocks.GET_COMMENT_1())
         // Assert
         assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_UPVOTED_COMMENT()))
         assertThat(mCommentsEndpointTd.mVoteWeight, `is`(1))
     }

    @Test
    fun upVoteAndNotify_downVotedCommentCorrectCommentAndVoteWeightPassedToEndpoint() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_DOWNVOTED_COMMENT())
        // Assert
        assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_UPVOTED_COMMENT()))
        assertThat(mCommentsEndpointTd.mVoteWeight, `is`(1))
    }

    @Test
    fun upVoteAndNotify_upVotedCommentCorrectCommentAndVoteWeightPassedToEndpoint() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_UPVOTED_COMMENT())
        // Assert
        assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_COMMENT_1()))
        assertThat(mCommentsEndpointTd.mVoteWeight, `is`(0))
    }

    @Test
    fun upVoteAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun upVoteAndNotify_success_localCommentUpdatedInCache() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        assertThat(mCommentCacheTd.mUpdateLocalCacheCounter, `is`(1))
        assertThat(
            mCommentCacheTd.mUpdatedComment, `is`(CommentMocks.GET_UPVOTED_COMMENT())
        )
    }

    @Test
    fun upVoteAndNotify_success_listenersNotifiedOfSuccess() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onVoteUpdate()
        verify(mListenerMock2).onVoteUpdate()
    }

    @Test
    fun upVoteAndNotify_success_unsubscribedListenersNotNotifiedOfSuccess() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onVoteUpdate()
        verify(mListenerMock2, never()).onVoteUpdate()
    }

    @Test
    fun upVoteAndNotify_error_ListenersNotifiedOfError() {
        //Arrange
        voteFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.upVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onVoteUpdateFailed()
        verify(mListenerMock2).onVoteUpdateFailed()
    }

    //-----------------

    @Test
    fun downVoteAndNotify_unvotedCommentCorrectCommentAndVoteWeightPassedToEndpoint() {
        // Arrange
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_DOWNVOTED_COMMENT()))
        assertThat(mCommentsEndpointTd.mVoteWeight, `is`(-1))
    }

    @Test
    fun downVoteAndNotify_upVotedCommentCorrectCommentAndVoteWeightPassedToEndpoint() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_UPVOTED_COMMENT())
        // Assert
        assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_DOWNVOTED_COMMENT()))
        assertThat(mCommentsEndpointTd.mVoteWeight, `is`(-1))
    }

    @Test
    fun downVoteAndNotify_downVotedCommentCorrectCommentAndVoteWeightPassedToEndpoint() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_DOWNVOTED_COMMENT())
        // Assert
        assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_COMMENT_1()))
        assertThat(mCommentsEndpointTd.mVoteWeight, `is`(0))
    }

    @Test
    fun downVoteAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun downVoteAndNotify_success_localCommentUpdatedInCache() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        assertThat(
            mCommentCacheTd.mUpdatedComment, `is`(CommentMocks.GET_DOWNVOTED_COMMENT())
        )
    }

    @Test
    fun downVoteAndNotify_success_listenersNotifiedOfSuccess() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onVoteUpdate()
        verify(mListenerMock2).onVoteUpdate()
    }

    @Test
    fun downVoteAndNotify_success_unsubscribedListenersNotNotifiedOfSuccess() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onVoteUpdate()
        verify(mListenerMock2, never()).onVoteUpdate()
    }

    @Test
    fun downVoteAndNotify_error_ListenersNotifiedOfError() {
        //Arrange
        voteFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.downVoteAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onVoteUpdateFailed()
        verify(mListenerMock2).onVoteUpdateFailed()
    }

    //-----------------

    @Test
    fun deleteCommentAndNotify_correctCommentPassedToEndpoint() {
        //Arrange
        // Act
        SUT.deleteCommentAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        assertThat(mCommentsEndpointTd.mComment, `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun deleteCommentAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.deleteCommentAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun deleteCommentAndNotify_success_commentDeletedFromLocalCache() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.deleteCommentAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        assertThat(
            mCommentCacheTd.mDeleteCommentsCounter, `is`(1)
        )
    }

    @Test
    fun deleteCommentAndNotify_success_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.deleteCommentAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onDeleteComment()
        verify(mListenerMock2).onDeleteComment()
    }

    @Test
    fun deleteCommentAndNotify_success_unsubscribedListenersNotNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.deleteCommentAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onDeleteComment()
        verify(mListenerMock2, never()).onDeleteComment()
    }

    @Test
    fun deleteCommentAndNotify_error_ListenersNotifiedOfError() {
        // Arrange
        deleteFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.deleteCommentAndNotify(CommentMocks.GET_COMMENT_1())
        // Assert
        verify(mListenerMock1).onDeleteCommentFailed()
        verify(mListenerMock2).onDeleteCommentFailed()
    }

    // region helper methods
    private fun voteSuccess() {
        // no-op because mFailure false by default
    }

    private fun voteFailure() {
        mCommentsEndpointTd.mFailure = true
    }

    private fun deleteSuccess() {
        // no-op because mFailure false by default
    }

    private fun deleteFailure() {
        mCommentsEndpointTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    class CommentsEndpointTd : CommentsEndpoint(null, null) {

        lateinit var mComment: Comment
        var mVoteWeight: Int = 0
        var mFailure = false

        override fun voteComment(comment: Comment, voteWeight: Int, listener: VoteListener) {
            mComment = comment
            mVoteWeight = voteWeight

            if (!mFailure) {
                listener.onVoteSuccess(comment)
            } else {
                listener.onVoteFailed()
            }
        }

        override fun deleteCommentVote(comment: Comment, listener: VoteListener) {
            mComment = comment

            if (!mFailure) {
                listener.onVoteSuccess(comment)
            } else {
                listener.onVoteFailed()
            }
        }

        override fun deleteComment(comment: Comment, listener: DeleteListener) {
            mComment = comment

            if (!mFailure) {
                listener.onDeleteSuccess()
            } else {
                listener.onDeleteFailed()
            }
        }
    }
    // endregion helper classes

}