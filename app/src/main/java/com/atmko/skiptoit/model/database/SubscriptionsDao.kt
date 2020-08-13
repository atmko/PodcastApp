package com.atmko.skiptoit.model.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.atmko.skiptoit.model.Podcast
import io.reactivex.Flowable

@Dao
interface SubscriptionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createSubscription(podcast: Podcast)

    @Query("SELECT EXISTS (SELECT 1 FROM subscriptions WHERE id = :podcastId)")
    fun isSubscribed(podcastId: String): LiveData<Boolean>

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flowable<List<Podcast>>

    @Query("DELETE FROM subscriptions WHERE id = :podcastId")
    fun deleteSubscription(podcastId: String)
}