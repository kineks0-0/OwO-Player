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
    private val albums: ArrayList<Album>
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
            // 仅 View 可见 和 LoadArt 为真时加载
            if (imageView.visibility == View.VISIBLE && MediaStoreProvider.loadAlbumArt) {
                Glide.with(imageView.context)
                    .load(MediaStoreProvider.getArt(album))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .transform(CenterCrop())
                    .into(imageView)
            }
        }
    }

}