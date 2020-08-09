package com.atmko.skiptoit.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import com.atmko.skiptoit.R
import com.atmko.skiptoit.view.MasterActivity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class DescriptionAdapter(val context: Context): PlayerNotificationManager.MediaDescriptionAdapter {
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