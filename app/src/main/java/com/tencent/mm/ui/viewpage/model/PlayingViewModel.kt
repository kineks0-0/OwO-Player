package com.tencent.mm.ui.viewpage.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.KeyEvent
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.tencent.mm.data.service.PlayService
import com.tencent.mm.R
import com.tencent.mm.data.locally.utils.MusicPlay
import com.tencent.mm.data.locally.Song
import com.tencent.mm.data.locally.utils.AudioMngHelper
import com.tencent.mm.getContext

class PlayingViewModel : ViewModel() {


    private val playMode = MusicPlay.playMode
    private val audioMngHelper = AudioMngHelper.newInstance()
    @SuppressLint("StaticFieldLeak")
    private var playService: PlayService? = null
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as PlayService.PlayServiceBind
            playService = binder.getServiceSelf()
            //serviceBind = binder
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    val playingFragmentTitle: MutableLiveData<String> by lazy { MutableLiveData() }

    init {
        playingFragmentTitle.value = getContext().resources.getString(R.string.app_name)
    }

    val song: ObservableField<Song> = ObservableField()
        get() {
            field.set(playMode.getPlayingSong(0))
            return field
        }

    val songNext: ObservableField<Song> = ObservableField()
        get() {
            field.set(
                playMode.getSong(
                    playMode.getNextSong(false, 0)
                )
            )
            return field
        }

    val songNext2: ObservableField<Song> = ObservableField()
        get() {
            field.set(
                playMode.getSong(
                    playMode.getNextSong(false, 1)
                )
            )
            return field
        }


    fun onPlayModeChange(view: View, playModeType: Int) {
        when (playModeType) {
            MusicPlay.SongLoop -> Snackbar.make(view, R.string.SongLoop, Snackbar.LENGTH_SHORT)
                .show()
            MusicPlay.ListLoop -> Snackbar.make(view, R.string.ListLoop, Snackbar.LENGTH_SHORT)
                .show()
            MusicPlay.ListPlay -> Snackbar.make(view, R.string.ListPlay, Snackbar.LENGTH_SHORT)
                .show()
            MusicPlay.RandomPlay -> Snackbar.make(view, R.string.RandomPlay, Snackbar.LENGTH_SHORT)
                .show()
        }
    }


    fun preSong(view: View) = playMode.previousSong(true, 0)
    fun playSong(view: View) = playMode.play()
    fun nextSong(view: View) = playMode.nextSong(true, 0)
    fun switchPlayMode(view: View) = playMode.switchPlayMode()
    fun nextSong(offset: Int): Boolean = playMode.getNextSong(false, offset) != -1

    /*fun volumeUp(view: View) {
        audioMngHelper.addVoice100()
    }

    fun volumeDown(view: View) {
        audioMngHelper.subVoice100()
    }*/

    fun onCreate(activity: Activity) {

    }

    fun onStart(activity: Activity) {
        if (playService == null) {
            Intent(getContext(), PlayService::class.java).also { intent ->
                activity.startService(intent)
                activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun onDestroy(activity: Activity) {
        playService?.let {
            activity.unbindService(connection)
            it.binder.onBindDestroy()
        }
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent?, activity: Activity): Boolean {
        return when(keyCode) {
            KeyEvent.KEYCODE_0 -> {
                false
            }
            KeyEvent.KEYCODE_1 -> {
                false
            }
            KeyEvent.KEYCODE_2 -> {
                audioMngHelper.addVoice100()
                true
            }
            KeyEvent.KEYCODE_3 -> {
                false
            }
            KeyEvent.KEYCODE_4 -> {
                playMode.previousSong(true,0)
                true
            }
            KeyEvent.KEYCODE_5 -> {
                playMode.play()
                true
            }
            KeyEvent.KEYCODE_6 -> {
                playMode.nextSong()
                true
            }
            KeyEvent.KEYCODE_7 -> {
                false
            }
            KeyEvent.KEYCODE_8 -> {
                audioMngHelper.subVoice100()
                true
            }
            KeyEvent.KEYCODE_9 -> {
                false
            }
            KeyEvent.KEYCODE_BACK -> {
                activity.finish()
                true
            }

            else -> false
        }
    }


}