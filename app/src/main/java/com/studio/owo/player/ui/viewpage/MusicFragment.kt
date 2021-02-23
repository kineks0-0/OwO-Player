package com.studio.owo.player.ui.viewpage

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tencent.mm.R
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.data.locally.utils.MusicPlay
import com.tencent.mm.getContext
import com.studio.owo.player.ui.viewpage.adapter.MusicItemRecyclerViewAdapter
import com.studio.owo.player.ui.viewpage.model.MusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class MusicFragment : Fragment(), OnPageSelectedChange {

    private var columnCount = 1
    private lateinit var viewModel: MusicViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
    }

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
                    columnCount <= 1 -> LinearLayoutManager(context).apply {
                        scrollToPositionWithOffset(
                            view.adapter?.itemCount ?: 0 - 1,
                            Integer.MIN_VALUE
                        )
                    }
                    else -> GridLayoutManager(context, columnCount).apply {
                        scrollToPositionWithOffset(
                            view.adapter?.itemCount ?: 0 - 1,
                            Integer.MIN_VALUE
                        )
                    }
                }
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
                    it.onClickListener = viewModel.onClick
                    it.onKeyListener = viewModel.getOnKeyListener(this, it.father())
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
                    //scrollToPosition(adapter.lastHasFocusHolder)
                    findViewHolderForAdapterPosition(adapter.lastHasFocusHolder)?.itemView?.let {
                        it.requestFocus()
                        it.clearFocus()
                        it.requestFocus()
                        thread {
                            sleep(20)
                            post {
                                it.clearFocus()
                                it.requestFocus()
                            }
                        }
                    }

                } else {
                    adapter.lastHasFocusHolder = adapter.notFocusHolder
                }
            }
        }

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