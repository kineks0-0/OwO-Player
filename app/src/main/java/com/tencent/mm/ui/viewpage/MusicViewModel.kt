package com.tencent.mm.ui.viewpage

import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.tencent.mm.data.locally.MusicPlay

class MusicViewModel : ViewModel() ,MusicItemRecyclerViewAdapter.OnClick{

    override fun onClick(position: Int, holder: MusicItemRecyclerViewAdapter.ViewHolder) {
        Snackbar.make(holder.itemView,position.toString(), Snackbar.LENGTH_SHORT).show()
        MusicPlay.playMode.playSong(position)
    }
}