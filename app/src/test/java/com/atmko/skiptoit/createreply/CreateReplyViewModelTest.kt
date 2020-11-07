package com.atmko.skiptoit.createreply

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.testclass.CommentCacheTd
import com.atmko.skiptoit.testdata.CommentMocks
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
class CreateReplyViewModelTest {

    // region constants
    val COMMENT_ID_1: String = "commentId1"
    val COMMENT_ID_2: String = "commentId2"
    val PARENT_ID: String = "parentId"
    val REPLY_BODY: String = "commentBody"
    // endregion constants

    // end region helper fields
    private lateinit var mCreateReplyEndpointTd: CreateReplyEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    @Mock
    lateinit var mListenerMock1: CreateReplyViewModel.Listener

    @Mock
    lateinit var mListenerMock2: CreateReplyViewModel.Listener
    // endregion helper fields

    private lateinit var SUT: CreateReplyViewModel

    @Before
    fun setup() {
        mCreateReplyEndpointTd =
            CreateReplyEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = CreateReplyViewModel(mCreateReplyEndpointTd, mCommentCacheTd)
    }

    @Test
    fun getCachedParentCommentAndNotify_correctCommentIdPassedToMethod() {
        // Arrange
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        assertThat(mCommentCacheTd.mCommentId, `is`(COMMENT_ID_1))
    }

    @Test
    fun getCachedParentCommentAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun getCachedParentCommentAndNotify_getCachedCommentSuccessNullCommentReturned_listenersNotifiedOfError() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mListenerMock1).onLoadParentCommentFailed()
        verify(mListenerMock2).onLoadParentCommentFailed()
    }

    @Test
    fun getCachedParentCommentAndNotify_getCachedCommentSuccessNonNullCommentReturned_commentSavedInViewModel() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        SUT.registerListener(mListenerMock1)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        assertThat(SUT.parentComment, `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun getCachedParentCommentAndNotify_getCachedCommentSuccessNonNullCommentReturned_listenersNotifiedOfSuccess() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        val ac: ArgumentCaptor<Comment> = ArgumentCaptor.forClass(Comment::class.java)
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mListenerMock1).onLoadParentComment(ac.kotlinCapture())
        verify(mListenerMock2).onLoadParentComment(ac.kotlinCapture())
        assertThat(ac.allValues[0], `is`(CommentMocks.GET_COMMENT_1()))
        assertThat(ac.allValues[1], `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun getCachedParentCommentAndNotify_loadCommentSuccessNonNullCommentReturned_secondCallReturnsFromSavedValue() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_2)
        // Assert
        assertThat(mCommentCacheTd.mGetCachedCommentCounter, `is`(1))
        assertThat(SUT.parentComment, `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun getCachedParentCommentAndNotify_loadCommentSuccessNonNullCommentReturned_listenersNotifiedOfError() {
        // Arrange
        getCachedCommentFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mListenerMock1).onLoadParentCommentFailed()
        verify(mListenerMock2).onLoadParentCommentFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun createReplyAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun createReplyAndNotify_correctParentIdAndReplyBodyPassedToEndPoint() {
        // Arrange
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        assertThat(mCreateReplyEndpointTd.mCreateReplyCounter, `is`(1))
        assertThat(mCreateReplyEndpointTd.mParentId, `is`(PARENT_ID))
        assertThat(mCreateReplyEndpointTd.mReplyBody, `is`(REPLY_BODY))
    }

    @Test
    fun createReplyAndNotify_endpointSuccess_correctParentIdPassedToGetLastReplyPageTracker() {
        // Arrange
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY) 
        // Assert
        assertThat(mCommentCacheTd.mGetLastReplyPageTrackerCounter, `is`(1))
        assertThat(mCommentCacheTd.mGetLastReplyPageTrackerArgParentId, `is`(PARENT_ID))
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNullTrackerReturned_correctCommentIdPassedToUpdateReplyCount() {
        // Arrange
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        assertThat(mCommentCacheTd.mIncreaseReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mIncreaseReplyCountArgCommentId, `is`(PARENT_ID))
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNullTrackerReturnedUpdateReplyCountSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNextPage_correctCommentIdPassedToUpdateReplyCount() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        assertThat(mCommentCacheTd.mIncreaseReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mIncreaseReplyCountArgCommentId, `is`(PARENT_ID))
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNextPageUpdateReplyCountSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNoNextPage_correctEpisodeIdPassedToDeleteReplyPage() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        getLastReplyPageTrackerNoNextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY) 
        // Assert
        assertThat(mCommentCacheTd.mDeleteAllRepliesInPageCounter, `is`(1))
        assertThat(mCommentCacheTd.mDeleteAllRepliesInPageArgEpisodeId, `is`(PARENT_ID))
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeletePageSuccess_correctCommentIdPassedToUpdateReplyCount() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        getLastReplyPageTrackerNoNextPage()
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        assertThat(mCommentCacheTd.mIncreaseReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mIncreaseReplyCountArgCommentId, `is`(PARENT_ID))
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeletePageSuccessUpdateReplyCountSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        getLastReplyPageTrackerNoNextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY) 
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeletePageSuccessUpdateReplyCountSuccess_unsubscribedObserversNotNotified() {
        //Arrange
        getLastReplyPageTrackerTrackerReturned()
        getLastReplyPageTrackerNoNextPage()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY) 
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2, never()).onReplyCreated()
    }

    @Test
    fun createCommentAndNotify_endpointError_listenersNotifiedOfError() {
        // Arrange
        endpointFailure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreateFailed()
        verify(mListenerMock2).onReplyCreateFailed()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerError_listenersNotifiedOfError() {
        // Arrange
        getLastReplyPageTrackerError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY) 
        // Assert
        verify(mListenerMock1).onPageTrackerFetchFailed()
        verify(mListenerMock2).onPageTrackerFetchFailed()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNullTrackerReturnedUpdateReplyCountError_listenersNotifiedOfUpdateCountError() {
        // Arrange
        updateReplyCountError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onUpdateReplyCountFailed()
        verify(mListenerMock2).onUpdateReplyCountFailed()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNullTrackerReturnedUpdateReplyCountError_listenerNotifiedOfReplyCreation() {
        // Arrange
        updateReplyCountError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNextPageUpdateReplyCountError_listenersNotifiedOfError() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        updateReplyCountError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onUpdateReplyCountFailed()
        verify(mListenerMock2).onUpdateReplyCountFailed()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNextPageUpdateReplyCountError_listenersNotifiedOfReplyCreated() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        updateReplyCountError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    @Test
    fun createReplyAndNotify_endpointSuccessGetLastReplyPageTrackerSuccessNonNullTrackerReturnedNoNextPageDeleteError_listenersNotifiedOfError() {
        // Arrange
        getLastReplyPageTrackerTrackerReturned()
        getLastReplyPageTrackerNoNextPage()
        deletePageError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY) 
        // Assert
        verify(mListenerMock1).onReplyPageDeleteFailed()
        verify(mListenerMock2).onReplyPageDeleteFailed()
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    fun updateParentCommentReplyCountAndNotify_correctCommentIdPassedToUpdateReplyCount() {
        // Arrange
        // Act
        SUT.updateParentCommentReplyCountAndNotify(PARENT_ID)
        // Assert
        assertThat(mCommentCacheTd.mIncreaseReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mIncreaseReplyCountArgCommentId, `is`(PARENT_ID))
    }

    @Test
    fun updateParentCommentReplyCountAndNotify_updateReplyCountSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.updateParentCommentReplyCountAndNotify(PARENT_ID)
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    @Test
    fun updateParentCommentReplyCountAndNotify_updateReplyCountError_listenersNotifiedOfSuccess() {
        // Arrange
        updateReplyCountError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.updateParentCommentReplyCountAndNotify(PARENT_ID)
        // Assert
        verify(mListenerMock1).onUpdateReplyCountFailed()
        verify(mListenerMock2).onUpdateReplyCountFailed()
    }

    @Test
    fun createReplyAndNotify_updateReplyCountError_listenersNotifiedOfReplyCreated() {
        // Arrange
        updateReplyCountError()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated()
        verify(mListenerMock2).onReplyCreated()
    }

    // region helper methods
    fun getCachedCommentNonNullCommentReturned() {
        mCommentCacheTd.mGetCachedCommentNullCommentReturned = false
    }

    fun getCachedCommentFailure() {
        mCommentCacheTd.mGetCachedCommentFailure = true
    }

    private fun endpointFailure() {
        mCreateReplyEndpointTd.mFailure = true
    }

    private fun getLastReplyPageTrackerTrackerReturned() {
        mCommentCacheTd.mGetLastReplyPageTrackerNullPageTracker = false
    }

    private fun getLastReplyPageTrackerNoNextPage() {
        mCommentCacheTd.mGetLastReplyPageTrackerNoNextPage = true
    }

    private fun getLastReplyPageTrackerError() {
        mCommentCacheTd.mGetLastReplyPageTrackerFailure = true
    }

    private fun deletePageError() {
        mCommentCacheTd.mDeleteAllRepliesInPageFailure = true
    }

    private fun updateReplyCountError() {
        mCommentCacheTd.mIncreaseReplyCountFailure = true
    }
    // endregion helper methods

    // region helper classes
    class CreateReplyEndpointTd : CreateReplyEndpoint(null, null) {

        var mCreateReplyCounter = 0
        var mParentId: String = ""
        var mReplyBody: String = ""
        var mFailure = false

        override fun createReply(
            parentId: String,
            replyBody: String,
            listener: Listener
        ) {
            mCreateReplyCounter++
            mParentId = parentId
            mReplyBody = replyBody
            if (!mFailure) {
                listener.onCreateSuccess(CommentMocks.GET_REPLY_WITHOUT_REPLIES())
            } else {
                listener.onCreateFailed()
            }
        }
    }
    // endregion helper classes
}