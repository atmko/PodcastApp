package com.atmko.skiptoit.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscriptionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createSubscription(podcast: Podcast)

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): LiveData<List<Podcast>>

    @Delete
    fun deleteSubscription(podcast: Podcast)
}