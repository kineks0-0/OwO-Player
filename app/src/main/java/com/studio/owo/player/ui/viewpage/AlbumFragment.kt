package com.studio.owo.player.ui.viewpage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.studio.owo.player.data.locally.Album
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.getContext
import com.studio.owo.player.ui.FragmentBackHandler
import com.studio.owo.player.ui.MainActivity
import com.studio.owo.player.ui.viewpage.adapter.AlbumItemRecyclerViewAdapter
import com.studio.owo.player.ui.viewpage.adapter.MusicItemRecyclerViewAdapter
import com.studio.owo.player.ui.viewpage.model.MusicViewModel
import com.tencent.mm.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class AlbumFragment : Fragment(), OnPageSelectedChange, FragmentBackHandler {

    private var columnCount = 2                     //项目列数
    private lateinit var viewModel: MusicViewModel  //这个Model用于复用RecyclerView的init和keyDown
    private val recyclerView get() = requireView().findViewById<RecyclerView>(R.id.albumFragment)
    val title: MutableLiveData<String> by lazy {
        MutableLiveData(
            com.studio.owo.player.getContext().resources.getString(
                R.string.title_albums
            )
        )
    }






    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { columnCount = it.getInt(ARG_COLUMN_COUNT) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.album_fragment, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.albumFragment)
        // Set the adapter
        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
            }
        }
        return view
    }





    private val onClick: (position: Int, view: View, data: Album, list: ArrayList<Album>) -> Unit =
        { position, view, data, _ ->

            Snackbar.make(view, position.toString(), Snackbar.LENGTH_SHORT).show()
            GlobalScope.launch(Dispatchers.Main) {
                val list = MediaStoreProvider.querySongs(data)
                (childFragmentManager.findFragmentByTag("MusicFragmentInAlbumFragment")
                        as MusicFragment).playList = { list }
            }
            recyclerView.clearFocus()
            recyclerView.visibility = View.INVISIBLE
            requireView().findViewById<View>(R.id.musicFragmentList).apply {
                visibility = View.VISIBLE
                requestFocus()
                title.value = data.title.get()
            }
        }

    override fun onBackPressed(): Boolean {
        requireView().findViewById<View>(R.id.musicFragmentList).apply {
            if (visibility == View.VISIBLE) {
                visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                this@AlbumFragment.onPageSelectedChange(true, -1)
                title.value = com.studio.owo.player.getContext().resources.getString(
                    R.string.title_albums
                )
                return true
            }
        }
        return false
    }



    override fun onStart() {
        super.onStart()
        //延迟到 onStart 协程加载
        GlobalScope.launch(Dispatchers.Main) {
            with(recyclerView) {
                val albums = MediaStoreProvider.queryAlbum()
                AlbumItemRecyclerViewAdapter(albums).let {
                    adapter = it
                    it.onClickListener = onClick
                    it.onKeyListener = viewModel.getOnKeyListener(this, it.father())
                }
            }
        }
    }

    //用于处理 RecyclerView 焦点位置（按键兼容）
    override fun onPageSelectedChange(hasFocus: Boolean, position: Int) {
        if (recyclerView is RecyclerView && recyclerView.visibility == View.VISIBLE) {
            with(recyclerView) {
                if (adapter == null) {
                    return@with
                }
                val adapter = (adapter as AlbumItemRecyclerViewAdapter)
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
        } else {
            (childFragmentManager.findFragmentByTag("MusicFragmentInAlbumFragment")
                    as MusicFragment).onPageSelectedChange(hasFocus, position)
        }
    }


    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            AlbumFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

}