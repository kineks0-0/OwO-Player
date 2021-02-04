package com.tencent.mm.ui.viewpage

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.tencent.mm.R
import com.tencent.mm.data.locally.utils.MediaStoreProvider
import com.tencent.mm.data.locally.utils.MusicPlay
import com.tencent.mm.getContext
import com.tencent.mm.ui.viewpage.adapter.MusicItemRecyclerViewAdapter
import com.tencent.mm.ui.viewpage.model.MusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


class MusicFragment : Fragment(), OnPageSelectedChange {

    private var columnCount = 1
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.music_fragment, container, false)
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.setHasFixedSize(true)
                view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                /*view.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus)
                        requestFocus()
                }*/
            }
        }
        return view
    }


    override fun onStart() {
        super.onStart()

        GlobalScope.launch(Dispatchers.Main) {
            with(view!! as RecyclerView) {
                val songs = MediaStoreProvider.querySongs()
                MusicItemRecyclerViewAdapter(songs).let {
                    adapter = it
                    it.onClickListener = viewModel
                    it.onKeyListener = View.OnKeyListener { v, keyCode, event ->
                        when (keyCode) {
                            KeyEvent.KEYCODE_3 -> {
                                var position = getChildAdapterPosition(v)
                                position -= childCount - 1
                                viewModel.scrollToPosition(this,position)
                                thread {
                                    Thread.sleep(300)
                                    v.post {
                                        requestFocus()
                                        viewModel.scrollToPosition(this,position)
                                    }
                                }
                                true
                            }
                            KeyEvent.KEYCODE_9 -> {
                                var position = getChildAdapterPosition(v)
                                position += childCount - 1
                                viewModel.scrollToPosition(this,position)
                                thread {
                                    Thread.sleep(300)
                                    v.post {
                                        requestFocus()
                                        viewModel.scrollToPosition(this,position)
                                    }
                                }
                                true
                            }
                            else -> false
                        }
                    }
                }
                MusicPlay.playMode.update(songs)
            }
        }
    }

    override fun onPageSelectedChange(hasFocus: Boolean, position: Int) {
        if (view is RecyclerView) {
            with((view as RecyclerView)) {
                if (adapter == null) {
                    return@with
                }
                val adapter = adapter as MusicItemRecyclerViewAdapter
                if (hasFocus) {
                    viewModel.scrollToPosition(this,adapter.lastHasFocusHolder)
                } else {
                    adapter.lastHasFocusHolder = adapter.notFocusHolder
                }
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        var title = getContext().resources.getString(R.string.app_name)

        @JvmStatic
        fun newInstance(columnCount: Int) =
            MusicFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

}