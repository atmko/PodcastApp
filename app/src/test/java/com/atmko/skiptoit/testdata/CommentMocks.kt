package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Comment

class CommentMocks {

    companion object {
        val COMMENT_ID_1: String = "commentId1"
        val COMMENT_ID_2: String = "commentId2"
        val PARENT_ID_NULL: String = "parentId"
        val PARENT_ID: String = "parentId"
        val EPISODE_ID: String = "episodeId"
        val USERNAME: String = "username"
        val BODY: String = "body"
        val VOTE_TALLY_0: Int = 0
        val IS_USER_COMMENT: Boolean = false
        val VOTE_WEIGHT_0: Int = 0
        val REPLIES: Int = 0
        val TIMESTAMP: Long = 1000000
        val PROFILE_IMAGE: String = "profileImage"

        val BODY_UPDATE: String = "bodyUpdate"
        val VOTE_WEIGHT_1: Int = 1
        val VOTE_WEIGHT_NEG1: Int = -1
        val VOTE_TALLY_1: Int = 1

        fun GET_COMMENT_1(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID_NULL,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY_0,
                IS_USER_COMMENT,
                VOTE_WEIGHT_0,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_COMMENT_2(): Comment {
            return Comment(
                COMMENT_ID_2,
                PARENT_ID_NULL,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY_0,
                IS_USER_COMMENT,
                VOTE_WEIGHT_0,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_COMMENT_WITH_UPDATED_BODY(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID_NULL,
                EPISODE_ID,
                USERNAME,
                BODY_UPDATE,
                VOTE_TALLY_0,
                IS_USER_COMMENT,
                VOTE_WEIGHT_0,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_UPVOTED_COMMENT(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID_NULL,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY_1,
                IS_USER_COMMENT,
                VOTE_WEIGHT_1,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_DOWNVOTED_COMMENT(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID_NULL,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY_1,
                IS_USER_COMMENT,
                VOTE_WEIGHT_NEG1,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_REPLY(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY_UPDATE,
                VOTE_TALLY_0,
                IS_USER_COMMENT,
                VOTE_WEIGHT_0,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }
    }

}