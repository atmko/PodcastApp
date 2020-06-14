package com.atmko.podcastapp.dependencyinjection

import com.atmko.podcastapp.model.PodcastsApi
import com.atmko.podcastapp.model.PodcastsService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class ApiModule {
    private val BASE_URL = "https://listen-api.listennotes.com/api/v2/"

    @Provides
    fun providePodcastApi(): PodcastsApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(PodcastsApi::class.java)
    }

    @Provides
    fun providePodcastService(): PodcastsService {
        return PodcastsService()
    }
}