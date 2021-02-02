package com.tencent.mm.ui.viewpage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.tencent.mm.R
import com.tencent.mm.data.locally.MediaStoreProvider
import com.tencent.mm.getContext
import com.tencent.mm.ui.viewpage.dummy.DummyContent

/**
 * A fragment representing a list of Items.
 */
class AlbumFragment : Fragment(),OnPageSelectedChange {

    private var columnCount = 2

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
        val view = inflater.inflate(R.layout.album_fragment, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                post {
                    adapter = AlbumItemRecyclerViewAdapter(MediaStoreProvider.queryAlbum())
                }
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.setHasFixedSize(true);
                view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            2 -> Glide.with(recyclerView).pauseRequests();
                            else -> Glide.with(recyclerView).resumeRequests();
                        }
                    }
                })
            }
        }
        return view
    }

    override fun onPageSelectedChange(hasFocus: Boolean, position: Int) {
        if (view is RecyclerView) {
            with((view as RecyclerView)) {
                val albumAdapter = (adapter as AlbumItemRecyclerViewAdapter)
                if (hasFocus) {
                    scrollToPosition(albumAdapter.lastHasFocusHolder)
                    val item = findViewHolderForAdapterPosition(albumAdapter.lastHasFocusHolder)
                    item?.itemView?.requestFocus() ?: Log.w(this@AlbumFragment::class.java.toString(),"item == null")
                } else {
                    albumAdapter.lastHasFocusHolder = albumAdapter.notFocusHolder
                    Log.d(this@AlbumFragment::class.java.toString(),"lastHasFocusHolder = " + albumAdapter.lastHasFocusHolder)
                }
            }
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        var title = getContext().resources.getString(R.string.app_name)

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            AlbumFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

}