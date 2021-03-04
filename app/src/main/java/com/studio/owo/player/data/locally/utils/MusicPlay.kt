package com.studio.owo.player.data.locally.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
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
    fun addOnPlayListener(onPlayListener: OnPlayListener): OnPlayListener {
        playListeners.add(onPlayListener)
        return onPlayListener
    }
    fun removePlayListener(onPlayListener: OnPlayListener) = playListeners.remove(onPlayListener)

    private val playBackListeners = ArrayList<PlayBackListener>()
    private val onPlayBackListener = PlayBackListener { status,playback ->
        playback.apply {
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
                    onViewRedraw -> listener.onViewRedraw()
                    onRest -> listener.onRest()
                    onError -> listener.onError()
                }
        }

    }

    fun addPlayBackListener(callback: (status: Int, playBack: PlayBackListener) -> Unit): PlayBackListener {
        return PlayBackListener(callback).apply {
            playBackListeners.add(this)
        }
    }
    fun removePlayBackListener(onPlayBackListener: PlayBackListener) = playBackListeners.remove(onPlayBackListener)

    /*private val onPlayListener: OnPlayListener = object : OnPlayListener {
        override fun onPlayBegins(playMode: PlayMode) {
            for (listener in playListeners)
                listener.onPlayBegins(playMode)
        }

        override fun onPlayStop() {
            for (listener in playListeners)
                listener.onPlayStop()
        }

        override fun onPlayEnd() {
            for (listener in playListeners)
                listener.onPlayEnd()
        }

        override fun onPlayPause() {
            for (listener in playListeners)
                listener.onPlayPause()
        }

        override fun onPlayContinues() {
            for (listener in playListeners)
                listener.onPlayContinues()
        }

        override fun onPlayModeChange(playModeType: Int) {
            for (listener in playListeners)
                listener.onPlayModeChange(playModeType)
        }

        override fun onViewRedraw() {
            for (listener in playListeners)
                listener.onViewRedraw()
        }

        override fun onRest() {
            for (listener in playListeners)
                listener.onRest()
        }

        override fun onError() {
            for (listener in playListeners)
                listener.onError()
        }
    }*/
    /*val headSetListener: HeadSetUtil.OnHeadSetListener = object : HeadSetUtil.OnHeadSetListener {
        override fun onClick() {
            //单击: 播放/暂停;
            Log.i(HeadSetUtil::class.java.name, "单击")
            playMode.play()
        }

        override fun onDoubleClick() {
            //双击: 下一首;
            Log.i(HeadSetUtil::class.java.name, "双击")
            playMode.next()
        }

        override fun onThreeClick() {
            //三击: 上一首;
            Log.i(HeadSetUtil::class.java.name, "三连击")
            playMode.previous()
        }
    }*/

    var playMode: PlayMode = PlayModeImp(onPlayBackListener)
    //由 PlayMode 完成播放控制和列表播放

    interface OnPlayListener {
        fun onPlayBegins(playMode: PlayMode)
        fun onPlayStop()
        fun onPlayEnd()
        fun onPlayPause()
        fun onPlayContinues()
        fun onPlayModeChange(playModeType: Int)
        fun onViewRedraw()
        fun onRest()
        fun onError()
    }


    class PlayBackListener(
        private val callback: (status: Int,playBack: PlayBackListener) -> Unit
    ) {
        val onPlayBegins = 1
        val onPlayStop = 2
        val onPlayEnd = 3
        val onPlayPause = 4
        val onPlayContinues = 5
        val onPlayModeChange = 6
        val onViewRedraw = 7
        val onRest = 8
        val onError = 9

        fun onStatusChange(status: Int) = callback.invoke(status,this)

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
        }

        fun pausePlay() {
            play(false)
            onPlayBackListener.onPlayPause()
        }

        fun stopPlay() {
            stop()
            onPlayBackListener.onPlayStop()
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

        fun previous(onUserDo: Boolean = false, offset: Int = 0) = play(getPreviousIndex(onUserDo, offset))
        fun next(onUserDo: Boolean = false, offset: Int = 0) = play(getNextIndex(onUserDo, offset))
        //fun previous() = previous(false, 0)
        //fun next() = next(false, 0)
    }


    /*private fun getNewMediaPlayer(): MediaPlayer {

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            //isPlaying = false
            over = true
            playMode.next()
            onPlayListener.onPlayEnd()
        }

        // 设置播放错误监听
        mediaPlayer.setOnErrorListener { mp, _, _ ->
            //isPlaying = false
            over = true
            onPlayListener.onError()
            mp.reset()
            onPlayListener.onRest()
            true
        }

        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
        mediaPlayer.setWakeMode(getContext(), PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setVolume(1.0F, 1.0F)
        mediaPlayer.isLooping = false

        return mediaPlayer
    }*/

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

class PlayModeImp(onPlayBackListener: MusicPlay.PlayBackListener) :
    MusicPlay.PlayMode(onPlayBackListener) {

    private val onPlayBack = MusicPlayer.OnPlayBackStatusChange { status,playback ->
        when (status) {
            playback.onPlayBegins -> {
                onPlayBackListener.onPlayBegins()
            }
            playback.onPlayEnd -> {
                onPlayBackListener.onPlayEnd()
                next(true)
            }
            playback.onRest -> {
                onPlayBackListener.onRest()
            }
            playback.onError -> {
                onPlayBackListener.onError()
            }
        }
    }

    private val player = AudioPlayer(onPlayBack)
    override val isPlaying get() = player.isPlaying
    val over: Boolean
        get() = !isPlaying

    override fun play(isPlay: Boolean) {
        if (isPlay)
            player.start()
        else
            player.pause()
    }

    override fun playSong(song: Song) {
        player.play(song)
    }

    override fun stop() = player.stop()

}