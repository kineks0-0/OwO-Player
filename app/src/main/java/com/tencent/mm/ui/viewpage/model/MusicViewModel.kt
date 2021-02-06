package com.tencent.mm.ui.viewpage.model

import android.app.Instrumentation
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.tencent.mm.data.locally.Song
import com.tencent.mm.data.locally.utils.MusicPlay
import com.tencent.mm.databinding.MusicItemFragmentBinding
import com.tencent.mm.ui.viewpage.adapter.BaseRecyclerViewAdapter
import com.tencent.mm.ui.viewpage.adapter.MusicItemRecyclerViewAdapter
import kotlin.concurrent.thread


class MusicViewModel : ViewModel() {

    val onClick: (position: Int, view: View, data: Song) -> Unit =
        { position, view, data ->
            Snackbar.make(view, position.toString(), Snackbar.LENGTH_SHORT).show()
            MusicPlay.playMode.playSong(data)
        }

    fun getOnKeyListener(
        recyclerView: RecyclerView,
        adapter: BaseRecyclerViewAdapter<ViewDataBinding, Any>
    ) =
        with(recyclerView) {

            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        //由于用户的操作，屏幕产生惯性滑动，停止加载图片
                        RecyclerView.SCROLL_STATE_SETTLING -> Glide.with(recyclerView.context)
                            .pauseRequests()
                        //当屏幕滚动且用户使用的触碰或手指还在屏幕上，停止加载图片
                        RecyclerView.SCROLL_STATE_DRAGGING -> Glide.with(recyclerView.context)
                            .pauseRequests()

                        RecyclerView.SCROLL_STATE_IDLE -> Glide.with(recyclerView.context)
                            .resumeRequests()
                    }
                }
            })

            View.OnKeyListener { v, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@OnKeyListener false
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        val position = getChildAdapterPosition(v)
                        if (position != 0) return@OnKeyListener false
                        scrollToPosition(this, adapter.itemCount - 1)
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        val position = getChildAdapterPosition(v)
                        if (position != adapter.itemCount - 1) return@OnKeyListener false
                        scrollToPosition(this, 0)
                        true
                    }
                    KeyEvent.KEYCODE_0 -> {
                        if (layoutManager!!.isSmoothScrolling) return@OnKeyListener false
                        if (getChildAdapterPosition(v) == 0)
                            scrollToPosition(this, adapter.itemCount - 1)
                        else
                            scrollToPosition(this, 0)

                        true
                    }
                    KeyEvent.KEYCODE_3 -> {
                        var position = getChildAdapterPosition(v)
                        position -= childCount - 1
                        scrollToPosition(this, position)
                        true
                    }
                    KeyEvent.KEYCODE_9 -> {
                        //viewModel.sendKey(KeyEvent.KEYCODE_DPAD_DOWN, childCount,10)
                        var position = getChildAdapterPosition(v)
                        position += childCount - 1
                        scrollToPosition(this, position)
                        true
                    }
                    else -> false
                }
            }
        }

    private fun scrollToPosition(recyclerView: RecyclerView, position: Int) {
        if (position == -1) return
        recyclerView.scrollToPosition(position)
        /*recyclerView.findViewHolderForAdapterPosition(position)
            ?.itemView?.requestFocus()
            ?: Log.e(this::class.java.toString(), "item == null")*/
        recyclerView.post {
            //recyclerView.requestFocus()
            recyclerView.findViewHolderForAdapterPosition(position)
                ?.itemView?.requestFocus()
                ?: Log.e(this::class.java.toString(), "item == null")
            recyclerView.scrollToPosition(position)
        }
    }

    fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        position: Int,
    ) {
        if (position == -1) return
        recyclerView.smoothScrollToPosition(position)
        recyclerView.findViewHolderForAdapterPosition(position)
            ?.itemView?.requestFocus()
            ?: Log.e(this::class.java.toString(), "item == null")
        recyclerView.post {
            recyclerView.requestFocus()
            recyclerView.findViewHolderForAdapterPosition(position)
                ?.itemView?.requestFocus()
                ?: Log.e(this::class.java.toString(), "item == null")
            recyclerView.scrollToPosition(position)
        }
        /*thread {

            var loop = true
            while (loop) {
                if (recyclerView.layoutManager!!.isSmoothScrolling) Thread.sleep(500)
                else loop = false
            }

            sendKey(KeyEvent.KEYCODE_DPAD_UP, 1,0)
            Thread.sleep(20)
            do {
                loop = !(adapter.lastHasFocusHolder == position ||
                        adapter.notFocusHolder == position ||
                        adapter.hasFocusHolder == position )
                if (loop) {
                    if (!recyclerView.hasFocus()) {
                        v.post {
                            recyclerView.requestFocus()
                        }
                    }
                    sendKey(KeyEvent.KEYCODE_DPAD_DOWN, 1,0)
                    Thread.sleep(50)
                    scrollToPosition(recyclerView,position)
                }

            } while (loop)
        }*/
    }


    private val inst by lazy { Instrumentation() }
    fun sendKey(key: Int, time: Int, sleepMillis: Long = 80) {
        thread {
            try {
                for (i in 0 until time) {
                    inst.sendKeyDownUpSync(key)
                    Thread.sleep(sleepMillis)
                }
            } catch (e: Exception) {
                Log.e(this@MusicViewModel::class.java.toString(), e.message, e)
            }
        }
    }
}