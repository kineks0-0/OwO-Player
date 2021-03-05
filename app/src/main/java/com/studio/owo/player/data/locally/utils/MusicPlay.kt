package com.studio.owo.player.data.locally.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import androidx.preference.PreferenceManager
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.getContext


object MusicPlay {

    private val TAG = javaClass.canonicalName

    /*private val mAudioManager: AudioManager by lazy {
        getContext().getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager
    }
    private val mAudioFocusChange: OnAudioFocusChangeListener by lazy {
        object : OnAudioFocusChangeListener {
            override fun onAudioFocusChange(focusChange: Int) {
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        //长时间丢失焦点,当其他应用申请的焦点为AUDIOFOCUS_GAIN时，
                        //会触发此回调事件，例如播放QQ音乐，网易云音乐等
                        //通常需要暂停音乐播放，若没有暂停播放就会出现和其他音乐同时输出声音
                        Log.d(TAG, "AUDIOFOCUS_LOSS")
                        //StopPlay();
                        playMode.pausePlay()

                        //释放焦点，该方法可根据需要来决定是否调用
                        //若焦点释放掉之后，将不会再自动获得
                        mAudioManager.abandonAudioFocus(this)
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        //短暂性丢失焦点，当其他应用申请AUDIOFOCUS_GAIN_TRANSIENT或AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE时，
                        //会触发此回调事件，例如播放短视频，拨打电话等。
                        //通常需要暂停音乐播放
                        playMode.pausePlay()
                        Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT")
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->                     //短暂性丢失焦点并作降音处理
                        Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        //当其他应用申请焦点之后又释放焦点会触发此回调
                        //可重新播放音乐
                        playMode.continuesPlay()
                        Log.d(TAG, "AUDIOFOCUS_GAIN")
                    }
                }
            }
        }
    }*/

    private val playListeners = ArrayList<OnPlayListener>()
    fun addOnPlayBackListener(onPlayListener: OnPlayListener): OnPlayListener {
        playListeners.add(onPlayListener)
        return onPlayListener
    }

    fun removePlayBackListener(onPlayListener: OnPlayListener) = playListeners.remove(onPlayListener)

    private val playBackListeners = ArrayList<PlayBackListener>()
    private val onPlayBackListener = PlayBackListener { status, playback ->
        playback.apply {
            playMode.isPlaying
            for (listener in playBackListeners)
                listener.onStatusChange(status)
            for (listener in playListeners)
                when (status) {
                    onPlayBegins -> listener.onPlayBegins(playback.playMode)
                    onPlayStop -> listener.onPlayStop()
                    onPlayEnd -> listener.onPlayEnd()
                    onPlayPause -> listener.onPlayPause()
                    onPlayContinues -> listener.onPlayContinues()
                    onPlayModeChange -> listener.onPlayModeChange(playback.playMode.getPlayModeID())
                    onRest -> listener.onRest()
                    onError -> listener.onError()
                    onViewRedraw -> listener.onViewRedraw()
                    onPlayButtonRedraw -> listener.onPlayButtonRedraw(playback.playMode.isPlaying)
                }
        }

    }

    fun addPlayBackListener(callback: (status: Int, playBack: PlayBackListener) -> Unit): PlayBackListener {
        return PlayBackListener(callback).apply {
            playBackListeners.add(this)
        }
    }

    fun removePlayBackListener(onPlayBackListener: PlayBackListener) =
        playBackListeners.remove(onPlayBackListener)

    var playMode: PlayMode =
        if (PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("useExoPlayer", true)
        )
            PlayModeExoImp(onPlayBackListener)
        else
            PlayModeMediaImp(onPlayBackListener)
    //由 PlayMode 完成播放控制和列表播放

    interface OnPlayListener {
        fun onPlayBegins(playMode: PlayMode)
        fun onPlayStop()
        fun onPlayEnd()
        fun onPlayPause()
        fun onPlayContinues()
        fun onPlayModeChange(playModeType: Int)
        fun onRest()
        fun onError()
        fun onViewRedraw()
        fun onPlayButtonRedraw(isPlaying: Boolean)
    }


    class PlayBackListener(
        private val callback: (status: Int, playBack: PlayBackListener) -> Unit
    ) {
        val onPlayBegins = 1
        val onPlayStop = 2
        val onPlayEnd = 3
        val onPlayPause = 4
        val onPlayContinues = 5
        val onPlayModeChange = 6
        val onRest = 7
        val onError = 8
        val onViewRedraw = 9
        val onPlayButtonRedraw = 10

        fun onStatusChange(status: Int) = callback.invoke(status, this)

        val playMode get() = MusicPlay.playMode
        fun onPlayBegins() = onStatusChange(onPlayBegins)
        fun onPlayStop() = onStatusChange(onPlayStop)
        fun onPlayEnd() = onStatusChange(onPlayEnd)
        fun onPlayPause() = onStatusChange(onPlayPause)
        fun onPlayContinues() = onStatusChange(onPlayContinues)
        fun onPlayModeChange() = onStatusChange(onPlayModeChange)
        fun onViewRedraw() = onStatusChange(onViewRedraw)
        fun onRest() = onStatusChange(onRest)
        fun onError() = onStatusChange(onError)
        fun onPlayButtonRedraw() = onStatusChange(onPlayButtonRedraw)
    }



    abstract class PlayMode(private val onPlayBackListener: PlayBackListener) {

        companion object {
            const val SongLoop = 0
            const val ListPlay = 1
            const val ListLoop = 2
            const val RandomPlay = 3
        }

        private var playModeID: Int = ListPlay
        fun getPlayModeID(): Int = playModeID

        abstract val isPlaying: Boolean
        var playingSong: Song? = null
        var index: Int = -1
            set(newIndex) {
                if (newIndex == field && playingSong != null) return
                if (playList.size != 0 && newIndex > -1 && newIndex < playList.size) {
                    field = newIndex
                    playingSong = playList[newIndex]
                } else {
                    field = 0
                    playingSong = null
                }
            }
        var songList: ArrayList<Song> = ArrayList()
        val playList: ArrayList<Song> = ArrayList()

        fun switchPlayMode() {
            playModeID = when (playModeID) {
                RandomPlay -> SongLoop
                else -> playModeID + 1
            }
            switchPlayMode(playModeID)
            onPlayBackListener.onPlayModeChange()
        }

        fun switchPlayMode(playModeID: Int) {
            this.playModeID = playModeID
            val where = playingSong
            //index = 0

            playList.clear()
            playList.addAll(songList)
            when (playModeID) {
                SongLoop -> {
                    if (where != null)
                        playList.indexOf(where)
                            .let {
                                index = if (it != -1) it
                                else {
                                    playList.remove(where)
                                    playList.add(index, where)
                                    index
                                }
                            }
                }
                ListLoop -> {
                    if (where != null)
                        playList.indexOf(where)
                            .let {
                                index = if (it != -1) it
                                else {
                                    playList.remove(where)
                                    playList.add(index, where)
                                    index
                                }
                            }
                }
                ListPlay -> {
                    if (where != null)
                        playList.indexOf(where)
                            .let {
                                index = if (it != -1) it
                                else {
                                    playList.remove(where)
                                    playList.add(index, where)
                                    index
                                }
                            }
                }
                RandomPlay -> {
                    playList.shuffle()
                    if (where != null) {
                        playList.remove(where)
                        playList.add(0, where)
                        index = 0
                    }
                }
            }
            onPlayBackListener.onViewRedraw()
        }

        fun update(songList: ArrayList<Song>, index: Int) {
            if (this.songList != songList)
                this.songList = songList
            this.index = index
            switchPlayMode(playModeID)
        }

        fun update(songList: ArrayList<Song>, index: Song) {
            if (this.songList != songList)
                this.songList = songList
            this.index = playList.indexOf(index)
            switchPlayMode(playModeID)
        }

        fun update(songList: ArrayList<Song>) {
            if (this.songList.size != 0) {
                if (playingSong == null) index = 0
                val song = getPlayingSong(0)
                playList.clear()
                playList.addAll(songList)
                playList.lastIndexOf(song)
                    .let {
                        if (it != -1) index = it
                    }
                this.songList = songList
                switchPlayMode(playModeID)
            } else {
                this.songList = songList
                switchPlayMode(playModeID)
                index = -1
                playingSong = null
            }
        }

        fun update(index: Int) {
            this.index = index
        }

        fun update(index: Song) {
            if (index.file.get()!!.exists())
                this.index = playList.indexOf(index)
        }

        fun getSongIndex(): Int = index

        fun play() {
            if (playingSong == null || index == -1) {
                if (playList.size != 0) play(0)
                return
            }
            if (isPlaying)
                pausePlay()
            else {
                continuesPlay()
                if (!isPlaying)
                    if (playList.size != 0)
                        play(0)
            }
        }

        fun play(songList: ArrayList<Song>, index: Int) {
            update(songList, index)
            play(this.index)
        }

        fun play(index: Song) {
            play(playList.indexOf(index))
        }

        fun play(index: Int) {
            this.index = index
            if (playingSong == null) {
                onPlayBackListener.onError()
                return
            }
            val song: Song = playingSong ?: playList[index]
            if (isPlaying) stop()
            playSong(song)
            onPlayBackListener.onViewRedraw()
        }

        fun continuesPlay() {
            if (playingSong == null) {
                play(0)
                return
            }
            play(true)
            onPlayBackListener.onPlayContinues()
            onPlayBackListener.onPlayButtonRedraw()
        }

        fun pausePlay() {
            play(false)
            onPlayBackListener.onPlayPause()
            onPlayBackListener.onPlayButtonRedraw()
        }

        fun stopPlay() {
            stop()
            onPlayBackListener.onPlayStop()
            onPlayBackListener.onPlayButtonRedraw()
        }

        abstract fun playSong(song: Song)
        abstract fun play(isPlay: Boolean)
        abstract fun stop()

        fun getPreviousIndex(onUserDo: Boolean, offset: Int): Int {
            //auto = true
            return if (playModeID == SongLoop) {
                index
            } else {
                var previousPosition = index - 1 - offset
                if (previousPosition < 0)
                    previousPosition = if (onUserDo) {
                        playList.lastIndex
                    } else {
                        0
                    }
                previousPosition
            }
        }

        fun getNextIndex(onUserDO: Boolean, offset: Int): Int {
            return if (playModeID == SongLoop) {
                index
            } else {
                var nextPosition = index + 1 + offset
                if (nextPosition > playList.lastIndex) {
                    if (onUserDO || playModeID == ListLoop && playModeID == RandomPlay)
                        nextPosition = 0
                    else return -1 //ListPlay 在这里播放完停止,loop 则把 where 移回第一个继续
                }

                nextPosition
            }
        }

        fun getPlayingSong(offset: Int): Song? {
            if (playingSong == null) index = 0
            return if (index + offset < playList.size)
                playList[index + offset]
            else
                null
        }

        fun getSong(index: Int): Song? {
            if (index < 0 || index > playList.lastIndex) {
                return null
            }
            return playList[index]
        }

        fun previous(onUserDo: Boolean = false, offset: Int = 0) =
            play(getPreviousIndex(onUserDo, offset))
        fun next(onUserDo: Boolean = false, offset: Int = 0) = play(getNextIndex(onUserDo, offset))
        //fun previous() = previous(false, 0)
        //fun next() = next(false, 0)
    }


    /**
     * 通知android媒体库更新文件夹
     *
     * @param filePath ilePath 文件绝对路径，、/sda/aaa/jjj.jpg
     */
    fun scanFile(filePath: String, context: Context = getContext()) {
        try {
            MediaScannerConnection.scanFile(
                context, arrayOf(filePath), null
            ) { path, uri ->
                Log.i("*******", "Scanned $path:")
                Log.i("*******", "-> uri=$uri")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

class PlayModeMediaImp(onPlayBackListener: MusicPlay.PlayBackListener) :
    MusicPlay.PlayMode(onPlayBackListener) {

    private val onPlayBack = MusicPlayer.OnPlayBackStatusChange { status, playback ->
        when (status) {
            playback.onPlayBegins -> {
                onPlayBackListener.onViewRedraw()
                if (playback.loadFailed)
                    onPlayBackListener.onError()
                else
                    onPlayBackListener.onPlayBegins()
            }
            playback.onPlayEnd -> {
                onPlayBackListener.onPlayEnd()
                onPlayBackListener.onPlayButtonRedraw()
                next(true)
            }
            playback.onRest -> {
                onPlayBackListener.onRest()
                onPlayBackListener.onViewRedraw()
            }
            playback.onError -> {
                onPlayBackListener.onError()
                onPlayBackListener.onPlayButtonRedraw()
            }
            playback.onViewRedraw -> {
                onPlayBackListener.onViewRedraw()
            }
            playback.onPlayButtonRedraw -> {
                onPlayBackListener.onPlayButtonRedraw()
            }
        }
    }

    private val player = AudioPlayer(onPlayBack)
    override val isPlaying get() = player.isPlaying
    val over: Boolean
        get() = !isPlaying

    override fun play(isPlay: Boolean) {
        if (isPlay)
            player.play(true)
        else
            player.pause()
    }

    override fun playSong(song: Song) {
        player.play(song)
    }

    override fun stop() = player.stop()

}

class PlayModeExoImp(onPlayBackListener: MusicPlay.PlayBackListener) :
    MusicPlay.PlayMode(onPlayBackListener) {

    private val onPlayBack = MusicPlayer.OnPlayBackStatusChange { status, playback ->
        when (status) {
            playback.onPlayBegins -> {
                onPlayBackListener.onViewRedraw()
                if (playback.loadFailed)
                    onPlayBackListener.onError()
                else
                    onPlayBackListener.onPlayBegins()
            }
            playback.onPlayEnd -> {
                onPlayBackListener.onPlayEnd()
                onPlayBackListener.onPlayButtonRedraw()
                this.next(true)
            }
            playback.onRest -> {
                onPlayBackListener.onRest()
                onPlayBackListener.onViewRedraw()
            }
            playback.onError -> {
                onPlayBackListener.onError()
                onPlayBackListener.onPlayButtonRedraw()
            }
            playback.onViewRedraw -> {
                onPlayBackListener.onViewRedraw()
            }
            playback.onPlayButtonRedraw -> {
                onPlayBackListener.onPlayButtonRedraw()
            }
        }
    }

    private val player = ExoAudioPlayer(onPlayBack, getContext())
    override val isPlaying get() = player.isPlaying
    val over: Boolean
        get() = !isPlaying

    override fun play(isPlay: Boolean) = player.play(isPlay)

    override fun playSong(song: Song) {
        player.play(song)
    }

    override fun stop() = player.stop()

}