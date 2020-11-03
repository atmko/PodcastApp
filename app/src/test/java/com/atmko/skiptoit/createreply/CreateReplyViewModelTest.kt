package com.atmko.skiptoit.createreply

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
class CreateReplyViewModelTest {

    // region constants
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
        assertThat(mCommentCacheTd.mUpdateReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mUpdateReplyCountArgCommentId, `is`(PARENT_ID))
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
        assertThat(mCommentCacheTd.mUpdateReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mUpdateReplyCountArgCommentId, `is`(PARENT_ID))
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
        assertThat(mCommentCacheTd.mUpdateReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mUpdateReplyCountArgCommentId, `is`(PARENT_ID))
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
        assertThat(mCommentCacheTd.mUpdateReplyCountCounter, `is`(1))
        assertThat(mCommentCacheTd.mUpdateReplyCountArgCommentId, `is`(PARENT_ID))
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
        mCommentCacheTd.mUpdateReplyCountFailure = true
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
                listener.onCreateSuccess(CommentMocks.GET_REPLY())
            } else {
                listener.onCreateFailed()
            }
        }
    }
    // endregion helper classes
}