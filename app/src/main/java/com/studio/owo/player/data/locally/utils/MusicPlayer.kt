package com.studio.owo.player.data.locally.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.getContext
import com.studio.owo.player.getMimeType
import java.io.File


// 自定义播放接口 方便后面更改播放实现

abstract class MusicPlayer {

    init {
        init()
    }

    private fun init() {
        setting()
    }

    abstract fun setting(
        sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getContext())
    )

    abstract val isPlaying: Boolean
    abstract fun play(musicData: MusicData)     //为了后续扩展
    abstract fun play(song: Song)               //为了方便和避免多余的对象包装

    //abstract fun play(musicData: MusicData, playCallBack: PlayCallBack)
    abstract fun play()
    abstract fun play(isPlay: Boolean)

    //abstract fun start()
    abstract fun pause()
    abstract fun continues()
    abstract fun seekTo(ms: Long)
    abstract fun stop()
    //abstract fun setOnCompletionListener(Listener: (musicPlayer: MusicPlayer) -> Unit)
    //abstract fun setOnErrorListener(Listener: (musicPlayer: MusicPlayer, what: Int, extra: Int) -> Boolean)

    abstract fun reset()
    abstract fun release()

    //abstract fun <T> getPlayer(): T


    interface MusicData {
        fun getFile(): File
        fun getUri(): Uri
        fun getSong(): Song
        fun usePlayT(): Boolean
        fun play(musicPlayer: MusicPlayer): Boolean
    }

    /*interface PlayCallBack {
        fuonPlay(musicPlayer: MusicPlayer, musicData: MusicData)
        fun onPrepared(musicPlayer: MusicPlayer)
        fun onLoadFailed(musicPlayer: MusicPlayer)
    }*/

    class OnPlayBackStatusChange(private val callback: (status: Int, playback: OnPlayBackStatusChange) -> Unit) {
        val onPlayBegins = 1
        val onPlayStop = 2
        val onPlayEnd = 3
        val onPlayPause = 4
        val onPlayContinues = 5
        val onRest = 6
        val onError = 7
        val onViewRedraw = 8
        val onPlayButtonRedraw = 9

        var isPlaying = false
        var loadFailed = false

        //abstract fun getMusicPlayer(): MusicPlayer
        fun onStatusChange(status: Int) = callback.invoke(status, this)


        fun onPlayBegins(LoadFailed: Boolean) {
            onStatusChange(onPlayBegins)
            loadFailed = LoadFailed
        }

        fun onPlayStop() = onStatusChange(onPlayStop)
        fun onPlayEnd() = onStatusChange(onPlayEnd)
        fun onPlayPause() = onStatusChange(onPlayPause)
        fun onPlayContinues() = onStatusChange(onPlayContinues)
        fun onRest() = onStatusChange(onRest)
        fun onError() = onStatusChange(onError)
        fun onViewRedraw() = onStatusChange(onViewRedraw)
        fun onPlayButtonRedraw() = onStatusChange(onPlayButtonRedraw)
    }

}


// ExoPlayer 的包装类
class ExoAudioPlayer(private val onPlayBack: OnPlayBackStatusChange, context: Context) :
    MusicPlayer() {

    private val exoPlayer by lazy { getNewExoPlayer(context) }
    override val isPlaying get() = exoPlayer.isPlaying

    private fun getNewExoPlayer(context: Context): ExoPlayer {
        return SimpleExoPlayer.Builder(context)
            .build().apply {
                playWhenReady = true
                addListener(
                    object : Player.EventListener {
                        override fun onPlaybackStateChanged(state: Int) {
                            super.onPlaybackStateChanged(state)
                            if (state == Player.EVENT_PLAYBACK_STATE_CHANGED
                                || state == Player.EVENT_PLAY_WHEN_READY_CHANGED) {
                                onPlayBack.onViewRedraw()
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            super.onIsPlayingChanged(isPlaying)
                            onPlayBack.onPlayButtonRedraw()
                            if (isPlaying) {
                                onPlayBack.onPlayContinues()
                            } else
                                onPlayBack.onPlayPause()
                        }
                    }
                )
            }
    }


    init {

    }


    override fun setting(sharedPreferences: SharedPreferences) {

    }


    override fun play(musicData: MusicData) {
        if (musicData.usePlayT()) {
            musicData.play(this)//由 music data 负责,除非兼容性问题否则不会调用
        } else {

            try {
                // ExoPlayer 没有 rest 方法,这里为了兼容考虑保留回调
                onPlayBack.onRest()
                val mediaItem = MediaItem.fromUri(musicData.getUri())
                exoPlayer.setMediaItem(mediaItem)
                //exoPlayer.playWhenReady = true
                exoPlayer.prepare()
                //exoPlayer.play()
                onPlayBack.onPlayBegins(true)
                onPlayBack.isPlaying = true
            } catch (e: Exception) {
                Log.e(this.toString(), e.message, e)
                onPlayBack.isPlaying = false
                onPlayBack.onError()
                onPlayBack.onRest()
                onPlayBack.onPlayBegins(false)
                return
            }
        }
    }

    override fun play(song: Song) {
        try {
            onPlayBack.onRest()
            val file = song.file.get() ?: throw Exception("Song File NPE")
            val mediaItem = MediaItem.fromUri(file.toUri())
            //exoPlayer.playWhenReady = true
            /*MediaItem.Builder()
                .setUri(file.toUri())
                .setMimeType(file.getMimeType())
                .build()*/
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            //exoPlayer.play()
            // todo 音频焦点

            onPlayBack.isPlaying = true
            onPlayBack.onPlayBegins(true)
        } catch (e: Exception) {
            Log.e(this.toString(), e.message, e)
            onPlayBack.isPlaying = false
            onPlayBack.onError()
            onPlayBack.onRest()
            onPlayBack.onPlayBegins(false)
            return
        }
    }

    override fun play() {
        if (isPlaying)
            exoPlayer.pause()
        else
            exoPlayer.play()
    }

    override fun play(isPlay: Boolean) {
        if (isPlay)
            exoPlayer.play()
        else
            exoPlayer.pause()
    }

    //fun start() = exoPlayer.play()
    override fun pause() = exoPlayer.pause()
    override fun continues() = exoPlayer.play()
    override fun seekTo(ms: Long) = exoPlayer.seekTo(ms)
    override fun stop() = exoPlayer.stop()

    override fun reset() = Unit // ExoPlayer 不需要 Rest
    override fun release() = exoPlayer.release()

    fun getPlayer() = exoPlayer

}


// media player 的包装类
class AudioPlayer(private val onPlayBack: OnPlayBackStatusChange) : MusicPlayer() {

    private val mediaPlayer by lazy { getNewMediaPlayer() }
    override val isPlaying get() = mediaPlayer.isPlaying

    private fun getNewMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setOnCompletionListener {
                onPlayBack.isPlaying = false
                //onPlayBack.over = true
                //onPlayBack.next()
                onPlayBack.onPlayEnd()
            }
            // 设置播放错误监听
            setOnErrorListener { mp, what, extra ->
                onPlayBack.isPlaying = false
                //onPlayBack.over = true
                onPlayBack.onError()
                //errorListener.invoke(this@AudioPlayer, what, extra)
                mp.reset()
                onPlayBack.onRest()
                true
            }
            // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
            setWakeMode(getContext(), PowerManager.PARTIAL_WAKE_LOCK)
            setVolume(1.0F, 1.0F)
            isLooping = false
        }
    }


    init {

    }


    override fun setting(sharedPreferences: SharedPreferences) {

    }


    override fun play(musicData: MusicData) {
        if (musicData.usePlayT()) {
            musicData.play(this)//由 music data 复制,除非兼容性问题否则不会调用
        } else {

            try {
                try {
                    mediaPlayer.reset()
                    onPlayBack.onRest()
                    mediaPlayer.setDataSource(getContext(), musicData.getUri())
                } catch (e: Exception) {
                    Log.e(this.toString(), e.message, e)
                    onPlayBack.isPlaying = false
                    onPlayBack.onError()
                    mediaPlayer.reset()
                    onPlayBack.onRest()
                    onPlayBack.onPlayBegins(false)

                    val file = musicData.getFile()
                    mediaPlayer.setDataSource(file.absolutePath)
                }
            } catch (e: Exception) {
                Log.e(this.toString(), e.message, e)
                onPlayBack.isPlaying = false
                onPlayBack.onError()
                mediaPlayer.reset()
                onPlayBack.onRest()
                onPlayBack.onPlayBegins(false)
                return
            }

            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {

                mediaPlayer.start()
                /*mAudioManager.requestAudioFocus(
                    MusicPlay.mAudioFocusChange,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )*/

                onPlayBack.isPlaying = true
                onPlayBack.onPlayBegins(true)
            }
        }
    }

    override fun play(song: Song) {
        try {
            mediaPlayer.reset()
            onPlayBack.onRest()
            val file = song.file.get() ?: throw Exception("Song File NPE")
            mediaPlayer.setDataSource(file.absolutePath)
        } catch (e: Exception) {
            Log.e(this.toString(), e.message, e)
            onPlayBack.isPlaying = false
            onPlayBack.onError()
            mediaPlayer.reset()
            onPlayBack.onRest()
            onPlayBack.onPlayBegins(false)
            return
        }
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            /*mAudioManager.requestAudioFocus(
                MusicPlay.mAudioFocusChange,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )*/
            // todo 音频焦点

            onPlayBack.isPlaying = true
            onPlayBack.onPlayBegins(true)
        }
    }

    override fun play() {
        if (isPlaying)
            mediaPlayer.pause()
        else
            mediaPlayer.start()
    }

    override fun play(isPlay: Boolean) {
        if (isPlay)
            mediaPlayer.start()
        else
            mediaPlayer.pause()
    }

    //fun start() = mediaPlayer.start()
    override fun pause() = mediaPlayer.pause()
    override fun continues() = mediaPlayer.start()
    override fun seekTo(ms: Long) = mediaPlayer.seekTo(ms.toInt())
    override fun stop() = mediaPlayer.stop()

    override fun reset() = mediaPlayer.reset()
    override fun release() = mediaPlayer.release()

    fun getPlayer() = mediaPlayer

}
