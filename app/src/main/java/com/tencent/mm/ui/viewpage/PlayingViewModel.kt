package com.tencent.mm.ui.viewpage

import android.view.KeyEvent
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.tencent.mm.R
import com.tencent.mm.data.locally.MusicPlay
import com.tencent.mm.data.locally.Song
import com.tencent.mm.getContext

class PlayingViewModel : ViewModel() {


    private val playMode = MusicPlay.playMode

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


    fun onClick(view: View) {

    }

    fun preSong(view: View) {
        playMode.previousSong(true, 0)
    }

    fun playSong(view: View) {
        playMode.play()
    }

    fun nextSong(view: View) {
        playMode.nextSong(true, 0)
    }

    fun switchPlayMode(view: View) {
        playMode.switchPlayMode()
    }

    fun nextSong(offset: Int): Boolean = playMode.getNextSong(false, offset) != -1

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when(keyCode) {
            KeyEvent.KEYCODE_0 -> {
                false
            }
            KeyEvent.KEYCODE_1 -> {
                false
            }
            KeyEvent.KEYCODE_2 -> {
                false
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
                false
            }
            KeyEvent.KEYCODE_9 -> {
                false
            }

            else -> false
        }
    }


}