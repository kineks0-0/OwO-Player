package com.studio.owo.player.ui.viewpage.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.studio.owo.player.R
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.databinding.MusicItemFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MusicItemRecyclerViewAdapter(val songs: List<Song>) :
    RecyclerView.Adapter<MusicItemRecyclerViewAdapter.ViewHolder>() {


    var onClickListener: OnClick = object : OnClick {
        override fun onClick(position: Int, holder: ViewHolder, adapter: MusicItemRecyclerViewAdapter) {}
    }
    var onKeyListener =  View.OnKeyListener { _, _, _ -> false }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MusicItemFragmentBinding.inflate(LayoutInflater.from(parent.context)
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
            onClickListener.onClick(holder.adapterPosition, holder, this)
        }
        binding.clickLayout.setOnKeyListener { _, keyCode, event ->
            onKeyListener.onKey(holder.itemView,keyCode,event)
        }
        return holder
    }

    private var hasFocusHolder = -1
    var notFocusHolder = -1
    var lastHasFocusHolder = -1

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.num = position.toString()
        holder.binding.song = songs[position]
    }

    override fun getItemCount(): Int = songs.size

    interface OnClick {
        fun onClick(position: Int, holder: ViewHolder, adapter: MusicItemRecyclerViewAdapter)
    }

    inner class ViewHolder(val binding: MusicItemFragmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        @BindingAdapter("android:music")
        @JvmStatic
        fun setSongImage(imageView: ImageView, song: Song) {
            GlobalScope.launch(Dispatchers.Main) {
                imageView.invalidate()
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