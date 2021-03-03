package com.studio.owo.player.data.service


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.studio.owo.player.OwOPlayerApplication.Companion.REQUEST_NOTIFICATION_CODE_NEXT_PLAY
import com.studio.owo.player.OwOPlayerApplication.Companion.REQUEST_NOTIFICATION_CODE_PAUSE_PLAY
import com.studio.owo.player.OwOPlayerApplication.Companion.REQUEST_NOTIFICATION_CODE_PREVIOUS_PLAY
import com.studio.owo.player.OwOPlayerApplication.Companion.SERVICE_NOTIFICATION_ID
import com.tencent.mm.R
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.data.locally.utils.MusicPlay
import com.studio.owo.player.OwOPlayerApplication.Companion.CHANNEL_SERVICE_IMPORTANCE
import com.studio.owo.player.data.locally.utils.MediaStoreProvider.UNKNOWN_ART_RES
import com.studio.owo.player.ui.MainActivity

class PlayService : Service() {

    private val playMode = MusicPlay.playMode
    val binder = PlayServiceBind()
    private val onPlayListener = MusicPlay.addOnPlayListener(object : MusicPlay.OnPlayListener {

        override fun onPlayBegins(song: Song, songList: ArrayList<Song>, index: Int) =
            onViewRedraw()

        override fun onPlayStop() = onViewRedraw()
        override fun onPlayEnd() = onViewRedraw()
        override fun onPlayPause() = onViewRedraw()
        override fun onPlayContinues() = onViewRedraw()
        override fun onRest() = onViewRedraw()
        override fun onError() = onViewRedraw()
        override fun onPlayModeChange(playModeType: Int) = onViewRedraw()
        override fun onViewRedraw() = onPlay()

    })

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.getIntExtra("requestCode", -1)) {
                REQUEST_NOTIFICATION_CODE_PREVIOUS_PLAY -> MusicPlay.playMode.previous()
                REQUEST_NOTIFICATION_CODE_PAUSE_PLAY -> MusicPlay.playMode.play()
                REQUEST_NOTIFICATION_CODE_NEXT_PLAY -> MusicPlay.playMode.next()
            }
        }
        return START_STICKY.takeIf { MusicPlay.isPlaying } ?: START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlay.removePlayListener(onPlayListener)
    }

    fun onPlay() {
        with(this@PlayService) {
            Glide.with(this).asBitmap().load(
                playMode.getPlayingSong(0)?.let {
                    MediaStoreProvider.getArtUri(it)
                } ?: UNKNOWN_ART_RES
            ).addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    createNotification(null)
                    Log.e(this@PlayService::class.java.toString(), e?.message ?: "", e)
                    return true
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    createNotification(resource)
                    return true
                }

            }
            ).preload()

        }

    }

    fun createNotification(artBitmap: Bitmap?) {
        val art = artBitmap ?: BitmapFactory.decodeResource(resources, UNKNOWN_ART_RES)

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_SERVICE_IMPORTANCE)

        val notification: Notification = notificationBuilder
            .setContentTitle(
                playMode.getPlayingSong(0)?.name?.get() ?: getString(R.string.app_name)
            )
            .setContentText(
                playMode.getPlayingSong(0)?.artist?.get()?.name?.get()
                    ?: getString(R.string.app_name)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
            .setLargeIcon(art.copy(art.config, art.isMutable))
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.app_name))
            .setOngoing(MusicPlay.isPlaying)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(
                        MediaSessionCompat(
                            this,
                            "MediaSession",
                            ComponentName(this@PlayService, Intent.ACTION_MEDIA_BUTTON),
                            null
                        ).sessionToken
                    ).setShowActionsInCompactView(0,1,2)
            )
            .addAction(
                R.drawable.ic_skip_previous_black_24dp, getString(R.string.previous_play),
                getPendingIntent(REQUEST_NOTIFICATION_CODE_PREVIOUS_PLAY)
            )
            .let {
                val intent = getPendingIntent(REQUEST_NOTIFICATION_CODE_PAUSE_PLAY)
                if (MusicPlay.isPlaying) {
                    it.addAction(
                        R.drawable.ic_pause_black_24dp, getString(R.string.pause), intent
                    )
                } else {
                    it.addAction(
                        R.drawable.ic_play_arrow_black_24dp, getString(R.string.play), intent
                    )
                }
                it
            }
            .addAction(
                R.drawable.ic_skip_next_black_24dp, getString(R.string.next_play),
                getPendingIntent(REQUEST_NOTIFICATION_CODE_NEXT_PLAY)
            )
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(
        requestCode: Int,
        flag: Int = PendingIntent.FLAG_CANCEL_CURRENT
    ): PendingIntent =
        Intent(this, this::class.java).let { notificationIntent ->
            notificationIntent.putExtra("requestCode", requestCode)
            PendingIntent.getService(this, requestCode, notificationIntent, flag)
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