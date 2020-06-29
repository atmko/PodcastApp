package com.atmko.podcastapp.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.JobIntentService
import androidx.core.graphics.drawable.toBitmap
import com.atmko.podcastapp.R
import com.atmko.podcastapp.view.MasterActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

const val NOTIFICATION_CHANNEL_ID_PLAYBACK = "Playback Channel"
const val NOTIFICATION_ID_PLAYBACK = 10
const val REQUEST_CODE_LAUNCH_MASTER_ACTIVITY = 1

class PlaybackService: JobIntentService() {
    private var oldUri: Uri? = null
    var player: SimpleExoPlayer? = null
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private var mBinder: PlaybackServiceBinder = PlaybackServiceBinder()
    private val intentFilter: IntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyReceiver = BecomingNoisyReceiver()

    override fun onHandleWork(intent: Intent) {

    }

    override fun onCreate() {
        super.onCreate()
        player = SimpleExoPlayer.Builder(baseContext).build()
        player?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(), true)
        }

        playerNotificationManager = PlayerNotificationManager(
            this,
            NOTIFICATION_CHANNEL_ID_PLAYBACK,
            NOTIFICATION_ID_PLAYBACK,
            DescriptionAdapter(baseContext)
        )
        playerNotificationManager.setPlayer(player)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerNotificationManager.setPlayer(null);
        player?.release()
        player = null
    }

    inner class BecomingNoisyReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                player?.playWhenReady = false
            }
        }
    }

    fun prepareMediaForPlayback(uri: Uri?, context: Context) {
        uri?.let {
            if (uri != oldUri) {
                // Produces DataSource instances through which media data is loaded.
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    context,
                    Util.getUserAgent(context, getString(R.string.app_name))
                )
                // This is the MediaSource representing the media to be played.
                val audioSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri)

                player?.prepare(audioSource)
            }

            oldUri = uri
        }
    }

    fun play(context: Context) {
        // Start the service
        startService(Intent(context, PlaybackService::class.java))
        player?.playWhenReady = true // this song was paused so we don't need to reload it

        // Register BECOME_NOISY BroadcastReceiver
        registerReceiver(noisyReceiver, intentFilter)
    }

    private class DescriptionAdapter(val context: Context): PlayerNotificationManager.MediaDescriptionAdapter {
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return PendingIntent.getActivity(
                context,
                REQUEST_CODE_LAUNCH_MASTER_ACTIVITY,
                Intent(context, MasterActivity::class.java), 0
            )
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return context.getString(R.string.app_name)
        }

        override fun getCurrentContentTitle(player: Player): CharSequence {
            return context.getString(R.string.app_name)
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.resources.getDrawable(R.drawable.ic_play_button_circular, null)?.toBitmap()
            } else {
                context.resources.getDrawable(R.drawable.ic_play_button_circular)?.toBitmap()
            }
        }
    }

    inner class PlaybackServiceBinder: Binder() {
        fun getService(): PlaybackService {
            return this@PlaybackService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}