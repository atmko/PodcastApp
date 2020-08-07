package com.atmko.skiptoit.dependencyinjection.application

import androidx.room.Room
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.model.database.SubscriptionsDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule  {

    @Provides
    @Singleton
    fun provideSubscriptionsDao(database: SkipToItDatabase): SubscriptionsDao {
        return database.subscriptionsDao()
    }

    @Provides
    @Singleton
    fun provideClientCacheDatabase(application: SkipToItApplication): SkipToItDatabase {
        return Room.databaseBuilder(
                application,
                SkipToItDatabase::class.java,
                "skip_to_it_database"
        ).build()
    }
}
