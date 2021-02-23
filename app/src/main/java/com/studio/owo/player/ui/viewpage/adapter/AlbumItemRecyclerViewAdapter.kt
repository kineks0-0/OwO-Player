package com.studio.owo.player.ui.viewpage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.tencent.mm.R
import com.studio.owo.player.data.locally.Album
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.tencent.mm.databinding.AlbumItemFragmentBinding


class AlbumItemRecyclerViewAdapter(
    private val albums: List<Album>
) : BaseRecyclerViewAdapter<AlbumItemFragmentBinding, Album>(albums) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.num = position.toString()
        holder.binding.album = albums[position]
    }

    override val inflate: (ViewGroup) -> AlbumItemFragmentBinding = {
        AlbumItemFragmentBinding.inflate(LayoutInflater.from(it.context), it, false)
    }
    override val clickLayout: (AlbumItemFragmentBinding) -> View = { it.clickLayout }
    companion object {
        @BindingAdapter("android:album")
        @JvmStatic
        fun setAlbumImage(imageView: ImageView, album: Album) {
            Glide.with(imageView.context)
                .load(MediaStoreProvider.getArt(album))
                .placeholder(R.drawable.unknown)
                .error(R.drawable.unknown)
                .transform(CenterCrop())
                .into(imageView)
        }
    }

}