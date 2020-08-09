package com.atmko.skiptoit.dependencyinjection.application

import android.content.IntentFilter
import android.media.AudioManager
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.services.BecomingNoisyReceiver
import com.atmko.skiptoit.services.DescriptionAdapter
import com.atmko.skiptoit.services.NOTIFICATION_CHANNEL_ID_PLAYBACK
import com.atmko.skiptoit.services.NOTIFICATION_ID_PLAYBACK
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
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
    fun providePlayerNotificationManager(
        skipToItApplication: SkipToItApplication,
        descriptionAdapter: DescriptionAdapter
    ): PlayerNotificationManager {
        return PlayerNotificationManager(
            skipToItApplication,
            NOTIFICATION_CHANNEL_ID_PLAYBACK,
            NOTIFICATION_ID_PLAYBACK,
            descriptionAdapter
        )
    }

    @Provides
    @Singleton
    fun provideMediaDescriptionAdapter(application: SkipToItApplication): DescriptionAdapter {
        return DescriptionAdapter(application)
    }

    @Provides
    @Singleton
    fun provideAudioBecomingNoisyIntentFilter(): IntentFilter {
        return IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }

    @Provides
    @Singleton
    fun provideBecomingNoisyReceiver(): BecomingNoisyReceiver {
        return BecomingNoisyReceiver()
    }
}