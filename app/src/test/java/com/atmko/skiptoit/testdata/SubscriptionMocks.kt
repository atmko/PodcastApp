package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Subscription

class SubscriptionMocks {

    companion object {
        const val LISTEN_NOTES_ID_1 = "podcastId1"
        const val LISTEN_NOTES_ID_2 = "podcastId2"
        const val LISTEN_NOTES_ID_3 = "podcastId3"
        const val LISTEN_NOTES_ID_4 = "podcastId4"

        fun GET_SUBSCRIPTION_1(): Subscription {
            return Subscription(LISTEN_NOTES_ID_1)
        }

        fun GET_SUBSCRIPTION_2(): Subscription {
            return Subscription(LISTEN_NOTES_ID_2)
        }

        fun GET_SUBSCRIPTIONS(): List<Subscription> {
            return listOf(GET_SUBSCRIPTION_1(), GET_SUBSCRIPTION_2())
        }
    }
}