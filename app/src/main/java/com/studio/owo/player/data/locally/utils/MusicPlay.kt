package com.studio.owo.player.data.locally.utils

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.os.PowerManager
import android.util.Log
import com.studio.owo.player.data.locally.Song
import com.tencent.mm.getContext


object MusicPlay {

    private val TAG = javaClass.canonicalName

    private var mediaPlayer = getNewMediaPlayer()

    //private var song: _root_ide_package_.com.studio.owo.player.data.locally.Song = _root_ide_package_.com.studio.owo.player.data.locally.Song(File("404"),-1L,-1L,-1L,"",-1,"")
    private val mAudioManager: AudioManager by lazy {
        getContext().getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager
    }
    private val mAudioFocusChange: OnAudioFocusChangeListener =
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

    var over = true
    val isPlaying get() = mediaPlayer.isPlaying

    //public var isPlaying = false
    private val playListeners = ArrayList<OnPlayListener>()

    fun addOnPlayListener(onPlayListener: OnPlayListener): OnPlayListener {
        playListeners.add(onPlayListener)
        return onPlayListener
    }

    fun removePlayListener(onPlayListener: OnPlayListener) = playListeners.remove(onPlayListener)

    private var onPlayListener: OnPlayListener = object : OnPlayListener {
        override fun onPlayBegins(song: Song, songList: ArrayList<Song>, index: Int) {
            for (listener in playListeners)
                listener.onPlayBegins(song, songList, index)
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
    }//默认接口

    val headSetListener: HeadSetUtil.OnHeadSetListener = object : HeadSetUtil.OnHeadSetListener {
        override fun onClick() {
            //单击: 播放/暂停;
            Log.i(HeadSetUtil::class.java.name, "单击")
            playMode.play()
        }

        override fun onDoubleClick() {
            //双击: 下一首;
            Log.i(HeadSetUtil::class.java.name, "双击")
            playMode.nextSong()
        }

        override fun onThreeClick() {
            //三击: 上一首;
            Log.i(HeadSetUtil::class.java.name, "三连击")
            playMode.previousSong()
        }
    }


    val SongLoop = 0
    val ListPlay = 1
    val ListLoop = 2
    val RandomPlay = 3

    var playMode: PlayMode = object : PlayMode {


        val SongLoop = 0
        val ListPlay = 1
        val ListLoop = 2
        val RandomPlay = 3
        //val playMode = 0

        private var playModeID: Int = ListPlay
        override fun getPlayModeID(): Int = playModeID

        var playingSong: Song? = null
        var index: Int = -1
            set(newIndex) {
                if (newIndex == field&&playingSong!=null) return
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
        //var auto: Boolean = false


        override fun switchPlayMode() {
            playModeID = when (playModeID) {
                RandomPlay -> SongLoop
                else -> playModeID + 1
            }
            switchPlayMode(playModeID)
            onPlayListener.onPlayModeChange(playModeID)
        }

        override fun switchPlayMode(playModeID: Int) {
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
            onPlayListener.onViewRedraw()
        }

        override fun update(songList: ArrayList<Song>, index: Int) {
            this.songList = songList
            this.index = index
            switchPlayMode(playModeID)
        }

        override fun update(songList: ArrayList<Song>) {
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

        override fun update(index: Int) {
            this.index = index
        }

        override fun update(index: Song) {
            if (index.file.get()!!.exists())
                this.index = playList.indexOf(index)
        }

        override fun getSongIndex(): Int = index

        override fun play() {
            if (playingSong == null || index == -1) {
                if (playList.size != 0) playSong(0)
                return
            }
            if (mediaPlayer.isPlaying)
                pausePlay()
            else {
                continuesPlay()
                /*if (!mediaPlayer.isPlaying)
                    if (playList.size != 0)
                        playSong(0)*/
            }
        }

        override fun play(songList: ArrayList<Song>, index: Int) {
            update(songList, index)
            playSong(this.index)
        }

        override fun playSong(index: Song) {
            playSong(playList.indexOf(index))
        }

        override fun playSong(index: Int) {

            this.index = index
            if (playingSong == null) {
                onPlayListener.onError()
                return
            }
            //if (index < 0 || index > playList.lastIndex || playList.size == 0) return
            over = false

            val song: Song = playingSong?:playList[index]



            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
            onPlayListener.onRest()

            try {
                val file = song.file.get()!!
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                //mediaPlayer.setDataSource(file.inputStream().fd,0,file.length())
                mediaPlayer.setDataSource(file.absolutePath)

            } catch (e: Exception) {
                Log.e(this@MusicPlay.toString(), e.message, e)
                //isPlaying = false
                over = true
                onPlayListener.onError()
                mediaPlayer.reset()
                onPlayListener.onRest()
                return
            }

            mediaPlayer.setOnPreparedListener {

                mediaPlayer.start()
                mAudioManager.requestAudioFocus(
                    mAudioFocusChange,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )

                //isPlaying = true
                over = false
                onPlayListener.onPlayBegins(song, playList, index)
                onPlayListener.onViewRedraw()
            }
            mediaPlayer.prepareAsync()
            //mediaPlayer.prepare()

        }


        override fun continuesPlay() {
            if (playingSong == null) {
                playSong(0)
                return
            }
            mediaPlayer.start()
            //isPlaying = true
            over = false
            onPlayListener.onPlayContinues()
        }

        override fun pausePlay() {
            //if (over) return
            mediaPlayer.pause()
            //isPlaying = false
            over = true
            onPlayListener.onPlayPause()
        }

        override fun stopPlay() {
            mediaPlayer.stop()
            over = true
            onPlayListener.onPlayStop()
        }

        override fun getPreviousSong(onUserDo: Boolean, offset: Int): Int {
            //auto = true
            return if (playModeID == SongLoop) {
                index
            } else {
                var previousPosition = index - 1 - offset
                if (previousPosition < 0) previousPosition = playList.lastIndex
                previousPosition
            }
        }

        override fun getNextSong(onUserDO: Boolean, offset: Int): Int {
            //auto = true
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

        override fun getPlayingSong(offset: Int): Song? {
            if (playingSong == null) index = 0
            return if (index + offset < playList.size)
                playList[index + offset]
            else
                null
        }

        override fun getSong(index: Int): Song? {
            if (index < 0 || index > playList.lastIndex) {
                return null
            }
            return playList[index]
        }

        override fun previousSong(onUserDo: Boolean, offset: Int) =
            playSong(getPreviousSong(onUserDo, offset))

        override fun nextSong(onUserDo: Boolean, offset: Int) =
            playSong(getNextSong(onUserDo, offset))

        override fun previousSong() = previousSong(false, 0)
        override fun nextSong() = nextSong(false, 0)

    }
//由 PlayMode 完成播放控制和列表播放

    interface OnPlayListener {
        fun onPlayBegins(song: Song, songList: ArrayList<Song>, index: Int)
        fun onPlayStop()
        fun onPlayEnd()
        fun onPlayPause()
        fun onPlayContinues()
        fun onPlayModeChange(playModeType: Int)
        fun onViewRedraw()
        fun onRest()
        fun onError()
    }

    interface PlayMode {

        fun switchPlayMode()
        fun switchPlayMode(playModeID: Int)
        fun getPlayModeID(): Int

        fun update(songList: ArrayList<Song>, index: Int)
        fun update(songList: ArrayList<Song>)
        fun update(index: Int)
        fun update(index: Song)
        fun getSongIndex(): Int

        fun play()
        fun play(songList: ArrayList<Song>, index: Int)
        fun pausePlay()
        fun continuesPlay()
        fun stopPlay()

        fun getPreviousSong(onUserDo: Boolean, offset: Int): Int
        fun getNextSong(onUserDO: Boolean, offset: Int): Int
        fun getPlayingSong(offset: Int): Song?
        fun getSong(index: Int): Song?

        fun previousSong(onUserDo: Boolean, offset: Int)
        fun previousSong()
        fun nextSong(onUserDo: Boolean, offset: Int)
        fun nextSong()
        fun playSong(index: Int)
        fun playSong(index: Song)

        /*companion object {
            const val SongLoop = 0
            const val ListPlay = 1
            const val ListLoop = 2
            const val RandomPlay = 3
            const val playMode = 0
        }*/
    }

    private fun getNewMediaPlayer(): MediaPlayer {

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            //isPlaying = false
            over = true
            playMode.nextSong()
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