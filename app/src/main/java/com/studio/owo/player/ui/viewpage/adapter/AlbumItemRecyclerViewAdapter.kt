package com.studio.owo.player.ui.viewpage.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.studio.owo.player.R
import com.studio.owo.player.data.locally.Album
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.databinding.AlbumItemFragmentBinding


class AlbumItemRecyclerViewAdapter(
    private val albums: List<Album>
) : RecyclerView.Adapter<AlbumItemRecyclerViewAdapter.ViewHolder>() {


    var onClickListener: OnClick = object : OnClick {
        override fun onClick(position: Int, holder: ViewHolder) {}
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AlbumItemFragmentBinding.inflate(LayoutInflater.from(parent.context)
            , parent, false)
        val holder = ViewHolder(binding)
        binding.clickLayout.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hasFocusHolder = holder.adapterPosition
            } else {
                notFocusHolder = holder.adapterPosition
            }
        }
        binding.clickLayout.setOnClickListener {
            onClickListener.onClick(holder.adapterPosition, holder)
        }

        return holder
    }

    private var hasFocusHolder = -1
    var notFocusHolder = -1
    var lastHasFocusHolder = -1

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val album = albums[position]
        holder.binding.num = position.toString()
        holder.binding.album = albums[position]
    }

    override fun getItemCount(): Int = albums.size

    companion object {
        @BindingAdapter("android:album")
        @JvmStatic
        fun setAlbumImage(imageView: ImageView, album: Album) {
            Glide.with(imageView.context)
                .load(MediaStoreProvider.getArt(album))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .transform(CenterCrop())
                .into(imageView)
        }
    }


    interface OnClick {
        fun onClick(position: Int, holder: ViewHolder)
    }

    inner class ViewHolder(val binding: AlbumItemFragmentBinding) :
        RecyclerView.ViewHolder(binding.root)

}