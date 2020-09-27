package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Comment

class CommentMocks {

    companion object {
        val COMMENT_ID_1: String = "commentId1"
        val COMMENT_ID_2: String = "commentId2"
        val PARENT_ID: String = "parentId"
        val EPISODE_ID: String = "episodeId"
        val USERNAME: String = "username"
        val BODY: String = "body"
        val VOTE_TALLY: Int = 0
        val IS_USER_COMMENT: Boolean = false
        val VOTE_WEIGHT: Int = 0
        val REPLIES: Int = 0
        val TIMESTAMP: Long = 1000000
        val PROFILE_IMAGE: String = "profileImage"

        val BODY_UPDATE: String = "bodyUpdate"

        fun GET_COMMENT_1(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY,
                IS_USER_COMMENT,
                VOTE_WEIGHT,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_COMMENT_2(): Comment {
            return Comment(
                COMMENT_ID_2,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY,
                IS_USER_COMMENT,
                VOTE_WEIGHT,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        fun GET_COMMENT_WITH_UPDATED_BODY(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY_UPDATE,
                VOTE_TALLY,
                IS_USER_COMMENT,
                VOTE_WEIGHT,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }
    }

}