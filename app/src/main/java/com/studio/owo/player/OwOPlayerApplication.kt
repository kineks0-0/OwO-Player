package com.studio.owo.player

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class OwOPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_SERVICE_IMPORTANCE;
            val channelName = "音乐通知";
            val importance = NotificationManager.IMPORTANCE_LOW
            this.createNotificationChannel(
                channelId,
                channelName,
                importance
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 10
        const val REQUEST_NOTIFICATION_CODE_PREVIOUS_PLAY = 20
        const val REQUEST_NOTIFICATION_CODE_PAUSE_PLAY = 21
        const val REQUEST_NOTIFICATION_CODE_NEXT_PLAY = 22
        const val SERVICE_NOTIFICATION_ID = 20
        //const val CHANNEL_DEFAULT_IMPORTANCE = ""
        const val CHANNEL_SERVICE_IMPORTANCE = "MusicPlay"
    }
}