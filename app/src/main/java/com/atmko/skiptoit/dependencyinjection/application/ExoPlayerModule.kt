package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.services.DescriptionAdapter
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ExoPlayerModule() {

    @Provides
    @Singleton
    fun provideExoPlayer(skipToItApplication: SkipToItApplication, audioAttributes: AudioAttributes): SimpleExoPlayer {
        val player = SimpleExoPlayer.Builder(skipToItApplication).build()
        return player.apply {
            setAudioAttributes(audioAttributes, true)
        }
    }

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaDescriptionAdapter(skipToItApplication: SkipToItApplication): DescriptionAdapter {
        return DescriptionAdapter(skipToItApplication)
    }
}