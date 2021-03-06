package com.atmko.skiptoit.services

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.atmko.skiptoit.R
import com.atmko.skiptoit.services.common.BaseService
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import javax.inject.Inject

const val NOTIFICATION_CHANNEL_ID_PLAYBACK = "Playback Channel"
const val NOTIFICATION_ID_PLAYBACK = 10
const val REQUEST_CODE_LAUNCH_MASTER_ACTIVITY = 1

class PlaybackService : BaseService() {

    private var mBinder: PlaybackServiceBinder = PlaybackServiceBinder()

    @Inject
    @JvmField
    var player: SimpleExoPlayer? = null

    @Inject
    lateinit var playerNotificationManager: PlayerNotificationManager

    @Inject
    lateinit var descriptionAdapter: DescriptionAdapter

    @Inject
    lateinit var intentFilter: IntentFilter

    @Inject
    lateinit var noisyReceiver: BecomingNoisyReceiver

    private var oldUri: Uri? = null

    private var isRegistered = false

    override fun onHandleWork(intent: Intent) {

    }

    override fun onCreate() {
        super.onCreate()
        getServiceComponent().inject(this)

        player!!.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    if (isRegistered) {
                        registerBecomingNoisyReceiver()
                        isRegistered = true
                    }
                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
                } else {
                    // player paused
                    if (isRegistered) {
                        unregisterBecomingNoisyReceiver()
                        isRegistered = false
                    }
                }
            }
        })

        playerNotificationManager.setPlayer(player)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerNotificationManager.setPlayer(null)
        player?.release()
        player = null
    }

    fun prepareMediaForPlayback(uri: Uri?) {
        uri?.let {
            if (uri != oldUri) {
                // Produces DataSource instances through which media data is loaded.
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    this,
                    Util.getUserAgent(this, getString(R.string.app_name))
                )
                // This is the MediaSource representing the media to be played.
                val audioSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri)

                player?.prepare(audioSource)
            }

            oldUri = uri
        }
    }

    fun play() {
        player?.playWhenReady = true // this song was paused so we don't need to reload it
    }

    fun restorePlayback(lastPlaybackPosition: Long) {
        if (!player!!.isPlaying) {
            player!!.seekTo(lastPlaybackPosition)
        }
    }

    fun togglePlayPause() {
        player?.let {
            it.playWhenReady = !it.isPlaying
        }
    }

    fun registerBecomingNoisyReceiver() {
        // Register BECOME_NOISY BroadcastReceiver
        registerReceiver(noisyReceiver, intentFilter)
    }

    fun unregisterBecomingNoisyReceiver() {
        // Unregister BECOME_NOISY BroadcastReceiver
        unregisterReceiver(noisyReceiver)
    }

    inner class PlaybackServiceBinder : Binder() {
        fun getService(): PlaybackService {
            return this@PlaybackService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}