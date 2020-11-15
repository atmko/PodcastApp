package com.atmko.skiptoit.dependencyinjection.application

import androidx.room.Room
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.model.database.*
import com.atmko.skiptoit.utils.AppExecutors
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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
    fun provideCommentsDao(database: SkipToItDatabase): CommentDao {
        return database.commentDao()
    }

    @Provides
    @Singleton
    fun provideEpisodeDao(database: SkipToItDatabase): EpisodeDao {
        return database.episodeDao()
    }


    @Provides
    @Singleton
    fun provideCommentsPageTrackerDao(database: SkipToItDatabase): CommentPageTrackerDao {
        return database.commentPageTrackerDao()
    }

    @Provides
    @Singleton
    fun provideSkipToItDatabase(application: SkipToItApplication): SkipToItDatabase {
        return Room.databaseBuilder(
                application,
                SkipToItDatabase::class.java,
                "skip_to_it_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAppExecutors(singleThreadExecutor: ExecutorService,
                            mainThreadExecutor: Executor
    ): AppExecutors {
        return AppExecutors(singleThreadExecutor, mainThreadExecutor)
    }

    @Provides
    @Singleton
    fun provideSingleThreadExecutor(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }

    @Provides
    @Singleton
    fun provideMainThreadExecutor(skipToItApplication: SkipToItApplication): Executor {
        return skipToItApplication.mainExecutor
    }
}
