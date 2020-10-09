package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.Subscription

class SubscriptionMocks {

    companion object {
        const val LISTEN_NOTES_ID_1 = "listenNotesId1"
        const val LISTEN_NOTES_ID_2 = "listenNotesId2"

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