package com.studio.owo.player.ui.viewpage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.data.locally.utils.MediaStoreProvider.UNKNOWN_ART_RES
import com.tencent.mm.databinding.MusicItemFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MusicItemRecyclerViewAdapter(songs: ArrayList<Song>) :
    BaseRecyclerViewAdapter<MusicItemFragmentBinding, Song>(songs) {


    override fun onBindViewHolder(
        holder: BaseRecyclerViewAdapter<MusicItemFragmentBinding, Song>.ViewHolder, position: Int
    ) {
        holder.binding.num = position.toString()
        holder.binding.song = list[position]
    }

    override val inflate: (ViewGroup) -> MusicItemFragmentBinding = {
        MusicItemFragmentBinding.inflate(LayoutInflater.from(it.context), it, false)
    }
    override val clickLayout: (binder: MusicItemFragmentBinding) -> View = { it.clickLayout }

    companion object {
        @BindingAdapter("android:music")
        @JvmStatic
        fun setSongImage(imageView: ImageView, song: Song) {

            // 仅 View 可见 和 LoadArt 为真时加载
            if (imageView.visibility == View.VISIBLE && MediaStoreProvider.loadAlbumArt) {
                GlobalScope.launch(Dispatchers.Main) {
                    Glide.with(imageView.context)
                        .load(MediaStoreProvider.getArtUri(song))
                        .placeholder(UNKNOWN_ART_RES)
                        .error(UNKNOWN_ART_RES)
                        .transform(CenterCrop(), RoundedCorners(4))
                        .into(imageView)
                }
            }

        }
    }



}