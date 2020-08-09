package com.atmko.skiptoit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.application.ApplicationModule
import com.atmko.skiptoit.dependencyinjection.application.DaggerApplicationComponent
import com.atmko.skiptoit.services.NOTIFICATION_CHANNEL_ID_PLAYBACK

class SkipToItApplication : Application() {

    val appComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        appComponent.inject(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.notification_channel_playback)
            val descriptionText = getString(R.string.channel_description_playback)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID_PLAYBACK, name, importance)
            notificationChannel.description = descriptionText
            notificationChannel.setSound(null,null)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}