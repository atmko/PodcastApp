package com.atmko.skiptoit.dependencyinjection

import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.SkipToItService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class SkipToItApiModule {
    private val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    @Provides
    fun provideSkipToItApi(): SkipToItApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(SkipToItApi::class.java)
    }

    @Provides
    fun provideSkipToItApiService(): SkipToItService {
        return SkipToItService()
    }
}