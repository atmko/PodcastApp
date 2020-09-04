package com.atmko.skiptoit.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.CommentPageTracker
import com.atmko.skiptoit.model.Podcast

@Database(entities = [Podcast::class, Comment::class, CommentPageTracker::class], version = 1)
abstract class SkipToItDatabase: RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: SkipToItDatabase? = null

        fun getInstance(context: Context): SkipToItDatabase {
            INSTANCE?.let {
                return it
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkipToItDatabase::class.java,
                    "skip_to_it_database"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun subscriptionsDao(): SubscriptionsDao
    abstract fun commentDao(): CommentDao
    abstract fun commentPageTrackerDao(): CommentPageTrackerDao
}