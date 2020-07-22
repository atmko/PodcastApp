package com.atmko.skiptoit.model

import androidx.room.*

@Dao
interface SubscriptionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createSubscription(podcast: Podcast)

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): List<Podcast>

    @Delete
    fun deleteSubscription(podcast: Podcast)
}