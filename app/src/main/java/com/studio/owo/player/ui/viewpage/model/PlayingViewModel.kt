package com.studio.owo.player.ui.viewpage.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
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
import com.studio.owo.player.getContext
import com.studio.owo.player.ui.MainActivity

class PlayingViewModel : ViewModel() {


    companion object {

        private val audioMngHelper = AudioMngHelper.newInstance()

        @SuppressLint("StaticFieldLeak")
        private var playService: PlayService? = null
        private var mBound: Boolean = false

        var lastCurrentItem = 0

    }





    // For DataBinding

    val song: ObservableField<Song> = ObservableField()
        get() {
            field.set(playMode.getPlayingSong(0))
            return field
        }

    val songNext: ObservableField<Song> = ObservableField()
        get() {
            field.set(
                playMode.getSong(
                    playMode.getNextIndex(false, 0)
                )
            )
            return field
        }

    val songNext2: ObservableField<Song> = ObservableField()
        get() {
            field.set(
                playMode.getSong(
                    playMode.getNextIndex(false, 1)
                )
            )
            return field
        }


    fun onPlayModeChange(view: View, playModeType: Int) {
        when (playModeType) {
            MusicPlay.PlayMode.SongLoop -> Snackbar.make(view, R.string.SongLoop, Snackbar.LENGTH_SHORT)
                .show()
            MusicPlay.PlayMode.ListLoop -> Snackbar.make(view, R.string.ListLoop, Snackbar.LENGTH_SHORT)
                .show()
            MusicPlay.PlayMode.ListPlay -> Snackbar.make(view, R.string.ListPlay, Snackbar.LENGTH_SHORT)
                .show()
            MusicPlay.PlayMode.RandomPlay -> Snackbar.make(view, R.string.RandomPlay, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    fun preSong(view: View) = playMode.previous(true, 0)
    fun playSong(view: View) = playMode.play()
    fun nextSong(view: View) = playMode.next(true, 0)
    fun switchPlayMode(view: View) = playMode.switchPlayMode()
    fun nextSong(offset: Int): Boolean = playMode.getNextIndex(false, offset) != -1

    fun volumeUp(view: View) {
        audioMngHelper.addVoice100()
        Snackbar.make(
            view,
            audioMngHelper.get100CurrentVolume().toString() + "%",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun volumeDown(view: View) {
        audioMngHelper.subVoice100()
        Snackbar.make(
            view,
            audioMngHelper.get100CurrentVolume().toString() + "%",
            Snackbar.LENGTH_SHORT
        ).show()
    }




    // For MainActivity

    fun onCreate(activity: Activity) {

    }

    fun onStart(activity: Activity) {
        if (playService == null && mBound) {
            Intent(getContext(), PlayService::class.java).also { intent ->
                activity.startService(intent)
            }
        }
    }

    fun onDestroy(activity: Activity) {
        try {
            playService?.binder?.onBindDestroy()
        } catch (e: Exception) {
            Log.e(this::javaClass.toString(), e.message, e)
        }
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent?, activity: MainActivity): Boolean {
        return with(activity) {
            when (keyCode) {

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // 无限循环页面
                    // 如果已经是 ViewPager 的第一页则跳转最后一页
                    if (binding.viewPage.currentItem == 0) {
                        binding.viewPage.currentItem = pagerAdapter.count - 1
                        true
                    } else {
                        false
                    }
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    // 无限循环页面
                    // 如果已经是 ViewPager 的最后一页则跳转第一页
                    if (binding.viewPage.currentItem == pagerAdapter.count - 1) {
                        binding.viewPage.currentItem = 0
                        true
                    } else {
                        false
                    }
                }

                KeyEvent.KEYCODE_0 -> false //这里由 RecyclerView 消费


                KeyEvent.KEYCODE_1 -> {     //静音
                    when (audioMngHelper.setAudioMute()) {
                        true -> Snackbar.make(
                            activity.binding.root,
                            R.string.now_mute, Snackbar.LENGTH_SHORT
                        ).show()
                        false -> Snackbar.make(
                            activity.binding.root,
                            R.string.now_unmute, Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                KeyEvent.KEYCODE_2 -> {     //音量+
                    audioMngHelper.addVoice100()
                    Snackbar.make(
                        activity.binding.root,
                        getString(R.string.now_voice100, audioMngHelper.get100CurrentVolume()),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    true
                }

                KeyEvent.KEYCODE_3 -> false     //这里由 RecyclerView 消费

                KeyEvent.KEYCODE_4 -> {     //上一首
                    playMode.previous(true, 0)
                    true
                }

                KeyEvent.KEYCODE_5 -> {     //播放暂停
                    playMode.play()
                    true
                }

                KeyEvent.KEYCODE_6 -> {     //下一首
                    playMode.next()
                    true
                }

                KeyEvent.KEYCODE_7 -> {     //跳转播放页
                    //如果不在播放页则跳转到播放页
                    //否则跳转回之前的页面 lastCurrentItem
                    if (activity.binding.viewPage.currentItem != 2) {
                        lastCurrentItem = activity.binding.viewPage.currentItem
                        activity.binding.viewPage.currentItem = 2
                    } else {
                        activity.binding.viewPage.currentItem = lastCurrentItem
                    }
                    true
                }

                KeyEvent.KEYCODE_8 -> {     //音量-
                    audioMngHelper.subVoice100()
                    Snackbar.make(
                        activity.binding.root,
                        getString(R.string.now_voice100, audioMngHelper.get100CurrentVolume()),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    true
                }

                KeyEvent.KEYCODE_9 -> false //这里由 RecyclerView 消费

                KeyEvent.KEYCODE_BACK -> {      //直接销毁
                    activity.onBackPressed()
                    true
                }

                else -> false
            }
        }

    }


}