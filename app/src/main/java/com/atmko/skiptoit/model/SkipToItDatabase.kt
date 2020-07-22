package com.atmko.skiptoit.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Podcast::class], version = 1)
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
}