package com.studio.owo.player.ui.viewpage.model

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
import com.studio.owo.player.data.service.PlayService
import com.tencent.mm.R
import com.studio.owo.player.data.locally.utils.MusicPlay
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.data.locally.utils.AudioMngHelper
import com.studio.owo.player.data.locally.utils.MusicPlay.playMode
import com.tencent.mm.getContext
import com.tencent.mm.ui.MainActivity

class PlayingViewModel : ViewModel() {


    companion object {
        //private val playMode = MusicPlay.playMode
        private val audioMngHelper = AudioMngHelper.newInstance()

        @SuppressLint("StaticFieldLeak")
        private var playService: PlayService? = null
        private var mBound: Boolean = false

        /** Defines callbacks for service binding, passed to bindService()  */
        private val connection by lazy {
            object : ServiceConnection {

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
        }

        var lastCurrentItem = 0
        val playingFragmentTitle: MutableLiveData<String> by lazy { MutableLiveData() }

        init {
            playingFragmentTitle.value = getContext().resources.getString(R.string.app_name)
        }
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

    fun volumeUp(view: View) {
        audioMngHelper.addVoice100()
        Snackbar.make(view,audioMngHelper.get100CurrentVolume().toString() + "%",Snackbar.LENGTH_SHORT).show()
    }

    fun volumeDown(view: View) {
        audioMngHelper.subVoice100()
        Snackbar.make(view,audioMngHelper.get100CurrentVolume().toString() + "%",Snackbar.LENGTH_SHORT).show()
    }

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

    fun onKeyDown(keyCode: Int, event: KeyEvent?, activity: MainActivity): Boolean {
        return with(activity) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (binding.viewPage.currentItem == 0) {
                        binding.viewPage.currentItem = pagerAdapter.count - 1
                        true
                    } else {
                        false
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (binding.viewPage.currentItem == pagerAdapter.count - 1) {
                        binding.viewPage.currentItem = 0
                        true
                    } else {
                        false
                    }
                }
                KeyEvent.KEYCODE_0 -> {
                    false
                }
                KeyEvent.KEYCODE_1 -> {
                    false
                }
                KeyEvent.KEYCODE_2 -> {
                    audioMngHelper.addVoice100()
                    Snackbar.make(activity.binding.root,audioMngHelper.get100CurrentVolume().toString() + "%",Snackbar.LENGTH_SHORT).show()
                    true
                }
                KeyEvent.KEYCODE_3 -> {
                    false
                }
                KeyEvent.KEYCODE_4 -> {
                    playMode.previousSong(true, 0)
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
                    if (activity.binding.viewPage.currentItem != 2) {
                        lastCurrentItem = activity.binding.viewPage.currentItem
                        activity.binding.viewPage.currentItem = 2
                    } else {
                        activity.binding.viewPage.currentItem = lastCurrentItem
                    }
                    true
                }
                KeyEvent.KEYCODE_8 -> {
                    audioMngHelper.subVoice100()
                    Snackbar.make(activity.binding.root,audioMngHelper.get100CurrentVolume().toString() + "%",Snackbar.LENGTH_SHORT).show()
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


}