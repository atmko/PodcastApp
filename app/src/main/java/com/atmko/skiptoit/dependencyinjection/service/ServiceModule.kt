package com.atmko.skiptoit.dependencyinjection.service

import android.app.Notification
import android.app.Service
import android.content.IntentFilter
import android.media.AudioManager
import com.atmko.skiptoit.R
import com.atmko.skiptoit.services.*
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import dagger.Module
import dagger.Provides

@Module
class ServiceModule(val service: Service) {

    @Provides
    fun providePlaybackService(): PlaybackService {
        return service as PlaybackService
    }

    @Provides
    fun provideAudioBecomingNoisyIntentFilter(): IntentFilter {
        return IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }

    @Provides
    fun provideBecomingNoisyReceiver(): BecomingNoisyReceiver {
        return BecomingNoisyReceiver()
    }

    @Provides
    fun providePlayerNotificationManager(
        playbackService: PlaybackService,
        descriptionAdapter: DescriptionAdapter
    ): PlayerNotificationManager {
        return PlayerNotificationManager.createWithNotificationChannel(
            playbackService,
            NOTIFICATION_CHANNEL_ID_PLAYBACK,
            R.string.notification_channel_playback,
            R.string.channel_description_playback,
            NOTIFICATION_ID_PLAYBACK,
            descriptionAdapter,
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    super.onNotificationPosted(notificationId, notification, ongoing)
                    playbackService.startForeground(notificationId, notification)
                }
            }
        )
    }
}