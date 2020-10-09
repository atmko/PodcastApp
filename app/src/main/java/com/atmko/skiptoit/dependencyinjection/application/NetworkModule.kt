package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.BuildConfig
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.model.PodcastsApi
import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {
    private val BASE_URL_LISTEN_NOTES = "https://listen-api.listennotes.com/api/v2/"
    private val BASE_URL_SKIP_TO_TI = "http://localhost:8080/api/v1/"

    @Provides
    @Singleton
    @Named("listen_notes")
    fun providePodcastApi(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_LISTEN_NOTES)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("skip_to_it")
    fun providePodcastApi2(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_SKIP_TO_TI)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePodcastService(
        @Named("listen_notes")retrofit: Retrofit): PodcastsApi {
        return retrofit.create(PodcastsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSkipToItApiService(
        @Named("skip_to_it")retrofit: Retrofit): SkipToItApi {
        return retrofit.create(SkipToItApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(googleSignInOptions: GoogleSignInOptions,
                                  skipToItApplication: SkipToItApplication): GoogleSignInClient {
        return GoogleSignIn.getClient(skipToItApplication, googleSignInOptions)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.googleServerId)
                .requestEmail()
                .build()
    }
}