package com.studio.owo.player.ui.viewpage.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.studio.owo.player.data.locally.utils.MusicPlay
import com.studio.owo.player.ui.viewpage.adapter.MusicItemRecyclerViewAdapter

class MusicViewModel : ViewModel() , MusicItemRecyclerViewAdapter.OnClick {

    override fun onClick(position: Int, holder: MusicItemRecyclerViewAdapter.ViewHolder, adapter: MusicItemRecyclerViewAdapter) {
        Snackbar.make(holder.itemView,position.toString(), Snackbar.LENGTH_SHORT).show()
        MusicPlay.playMode.playSong(adapter.songs[position])
    }

    fun scrollToPosition(recyclerView: RecyclerView,position: Int) {
        if (position==-1) return
        recyclerView.scrollToPosition(position)
        recyclerView.findViewHolderForAdapterPosition(position)
            ?.itemView?.requestFocus()
            ?: Log.w(this::class.java.toString(), "item == null")
    }
}