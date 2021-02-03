package com.studio.owo.player.data.service


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.studio.owo.player.OwOPlayerApplication.Companion.CHANNEL_SERVICE_IMPORTANCE
import com.studio.owo.player.OwOPlayerApplication.Companion.SERVICE_NOTIFICATION_ID
import com.studio.owo.player.R
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.data.locally.utils.MusicPlay
import com.studio.owo.player.ui.MainActivity

class PlayService : Service() {

    private val playMode = MusicPlay.playMode
    val binder = PlayServiceBind()
    private val onPlayListener = MusicPlay.addOnPlayListener(object : MusicPlay.OnPlayListener {

        override fun onPlayBegins(song: Song, songList: ArrayList<Song>, index: Int) = onPlay()
        override fun onPlayStop() {}
        override fun onPlayEnd() {}
        override fun onPlayPause() {}
        override fun onPlayContinues() {}
        override fun onRest() {}
        override fun onError() {}
        override fun onPlayModeChange(playModeType: Int) {}
        override fun onViewRedraw() {}

    })

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY.takeIf { MusicPlay.isPlaying } ?: START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun onPlay() {
        with(this@PlayService) {
            if (MusicPlay.isPlaying) {
                val pendingIntent: PendingIntent =
                    Intent(this, MainActivity::class.java).let { notificationIntent ->
                        PendingIntent.getActivity(this, 0, notificationIntent, 0)
                    }

                val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationCompat.Builder(this, CHANNEL_SERVICE_IMPORTANCE)
                } else NotificationCompat.Builder(this)

                val notification: Notification = notificationBuilder
                    .setContentTitle(playMode.getPlayingSong(0)?.name?.get() ?: getString(R.string.app_name))
                    .setContentText(playMode.getPlayingSong(0)?.artist?.get()?.name?.get() ?: getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.app_name))
                    .setOngoing(MusicPlay.isPlaying)
                    .build()

                startForeground(SERVICE_NOTIFICATION_ID, notification)
            }
        }
    }

    inner class PlayServiceBind : Binder() {

        fun getServiceSelf() = this@PlayService

        fun onBindDestroy() {
            if (MusicPlay.over) {
                this@PlayService.stopSelf()
                this@PlayService.stopForeground(true)
            }
        }


    }
}