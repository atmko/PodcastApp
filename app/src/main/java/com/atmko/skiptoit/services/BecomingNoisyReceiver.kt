package com.atmko.skiptoit.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.atmko.skiptoit.SkipToItApplication
import com.google.android.exoplayer2.SimpleExoPlayer
import javax.inject.Inject

class BecomingNoisyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var player: SimpleExoPlayer

    override fun onReceive(context: Context?, intent: Intent?) {
        (context as SkipToItApplication).appComponent.inject(this)

        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            player.playWhenReady = false
        }
    }
}