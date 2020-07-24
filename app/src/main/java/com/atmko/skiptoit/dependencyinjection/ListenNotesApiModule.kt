package com.atmko.skiptoit.dependencyinjection

import com.atmko.skiptoit.model.PodcastsApi
import com.atmko.skiptoit.model.SkipToItApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
class ListenNotesApiModule {
    private val BASE_URL = "https://listen-api.listennotes.com/api/v2/"
    private val BASE_URL2 = "http://10.0.2.2:8080/api/v1/"

    @Provides
    @Named("listen_notes")
    fun providePodcastApi(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Provides
    @Named("skip_to_it")
    fun providePodcastApi2(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Provides
    fun providePodcastService(
        @Named("listen_notes")retrofit: Retrofit): PodcastsApi {
        return retrofit.create(PodcastsApi::class.java)
    }

    @Provides
    fun provideSkipToItApiService(
        @Named("skip_to_it")retrofit: Retrofit): SkipToItApi {
        return retrofit.create(SkipToItApi::class.java)
    }
}