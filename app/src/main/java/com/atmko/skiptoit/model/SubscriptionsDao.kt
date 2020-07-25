package com.atmko.skiptoit.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscriptionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createSubscription(podcast: Podcast)

    @Query("SELECT EXISTS (SELECT 1 FROM subscriptions WHERE id = :podcastId)")
    fun isSubscribed(podcastId: String): LiveData<Boolean>

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): LiveData<List<Podcast>>

    @Query("DELETE FROM subscriptions WHERE id = :podcastId")
    fun deleteSubscription(podcastId: String)
}