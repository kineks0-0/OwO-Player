package com.studio.owo.player.data.locally.utils


import android.content.Context
import android.media.AudioManager
import androidx.annotation.IntDef
import com.studio.owo.player.getContext
import kotlin.math.ceil
import kotlin.math.floor


/**
 * <pre>
 * author: Chestnut
 * blog  : http://www.jianshu.com/u/a0206b5f4526
 * time  : 2017/6/17 16:11
 * desc  :  集成音量控制
 * thanks To:   http://blog.csdn.net/hufeng882412/article/details/7310131
 * dependent on:
 * update log:
</pre> *
 */
class AudioMngHelper(context: Context) {
    //private val TAG = "AudioMngHelper"
    //private val OpenLog = true
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var nowAudioType = TYPE_MUSIC
    private var nowFlag = FLAG_NOTHING
    private var voiceStep100 = 2 //0-100的步进。

    @IntDef(TYPE_MUSIC, TYPE_ALARM, TYPE_RING)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class TYPE

    @IntDef(FLAG_SHOW_UI, FLAG_PLAY_SOUND, FLAG_NOTHING)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class FLAG

    private val systemMaxVolume: Int
        get() = audioManager.getStreamMaxVolume(nowAudioType)
    private val systemCurrentVolume: Int
        get() = audioManager.getStreamVolume(nowAudioType)

    /**
     * 以0-100为范围，获取当前的音量值
     * @return  获取当前的音量值
     */
    fun get100CurrentVolume(): Int {
        return 100 * systemCurrentVolume / systemMaxVolume
    }

    /**
     * 修改步进值
     * @param step  step
     * @return  this
     */
    fun setVoiceStep100(step: Int): AudioMngHelper {
        voiceStep100 = step
        return this
    }

    /**
     * 改变当前的模式，对全局API生效
     * @param type
     * @return
     */
    fun setAudioType(@TYPE type: Int): AudioMngHelper {
        nowAudioType = type
        return this
    }

    /**
     * 改变当前FLAG，对全局API生效
     * @param flag
     * @return
     */
    fun setFlag(@FLAG flag: Int): AudioMngHelper {
        nowFlag = flag
        return this
    }

    fun addVoiceSystem(): AudioMngHelper {
        audioManager.adjustStreamVolume(nowAudioType, AudioManager.ADJUST_RAISE, nowFlag)
        return this
    }

    fun subVoiceSystem(): AudioMngHelper {
        audioManager.adjustStreamVolume(nowAudioType, AudioManager.ADJUST_LOWER, nowFlag)
        return this
    }

    /**
     * 调整音量，自定义
     * @param num   0-100
     * @return  改完后的音量值
     */
    fun setVoice100(num: Int): Int {
        var a = ceil(num * systemMaxVolume * 0.01).toInt()
        a = if (a <= 0) 0 else a
        a = if (a >= 100) 100 else a
        audioManager.setStreamVolume(nowAudioType, a, 0)
        return get100CurrentVolume()
    }

    /**
     * 步进加，步进值可修改
     * 0——100
     * @return  改完后的音量值
     */
    fun addVoice100(): Int {
        var a = ceil((voiceStep100 + get100CurrentVolume()) * systemMaxVolume * 0.01)
            .toInt()
        a = if (a <= 0) 0 else a
        a = if (a >= 100) 100 else a
        audioManager.setStreamVolume(nowAudioType, a, nowFlag)
        return get100CurrentVolume()
    }

    /**
     * 步进减，步进值可修改
     * 0——100
     * @return  改完后的音量值
     */
    fun subVoice100(): Int {
        var a = floor((get100CurrentVolume() - voiceStep100) * systemMaxVolume * 0.01)
            .toInt()
        a = if (a <= 0) 0 else a
        a = if (a >= 100) 100 else a
        audioManager.setStreamVolume(nowAudioType, a, nowFlag)
        return get100CurrentVolume()
    }

    companion object {

        fun newInstance() = AudioMngHelper(getContext())

        /**
         * 封装：STREAM_类型
         */
        const val TYPE_MUSIC = AudioManager.STREAM_MUSIC
        const val TYPE_ALARM = AudioManager.STREAM_ALARM
        const val TYPE_RING = AudioManager.STREAM_RING

        /**
         * 封装：FLAG
         */
        const val FLAG_SHOW_UI = AudioManager.FLAG_SHOW_UI
        const val FLAG_PLAY_SOUND = AudioManager.FLAG_PLAY_SOUND
        const val FLAG_NOTHING = 0
    }

}