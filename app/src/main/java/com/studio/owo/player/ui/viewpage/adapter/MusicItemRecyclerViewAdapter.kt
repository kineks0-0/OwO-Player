package com.studio.owo.player.ui.viewpage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tencent.mm.R
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.data.locally.Song
import com.tencent.mm.databinding.MusicItemFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MusicItemRecyclerViewAdapter(songs: List<Song>) :
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
            GlobalScope.launch(Dispatchers.Main) {
                Glide.with(imageView.context)
                    .load(MediaStoreProvider.getArtUri(song))
                    .placeholder(R.drawable.unknown)
                    .error(R.drawable.unknown)
                    .transform(CenterCrop(), RoundedCorners(4))
                    .into(imageView)
            }
        }
    }



}