package com.tencent.mm.ui.viewpage

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tencent.mm.R
import com.tencent.mm.data.locally.MediaStoreProvider
import com.tencent.mm.data.locally.Song
import com.tencent.mm.databinding.MusicItemFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MusicItemRecyclerViewAdapter(private val songs: List<Song>) :
    RecyclerView.Adapter<MusicItemRecyclerViewAdapter.ViewHolder>() {


    var onClickListener: OnClick = object : OnClick {
        override fun onClick(position: Int, holder: ViewHolder) {}
    }
    var onKeyListener =  View.OnKeyListener { v, keyCode, event ->
        false
    }


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
            onClickListener.onClick(holder.adapterPosition, holder)
        }
        binding.clickLayout.setOnKeyListener { v, keyCode, event ->
            onKeyListener.onKey(holder.itemView,keyCode,event)
        }
        return holder
    }

    private var hasFocusHolder = -1
    var notFocusHolder = -1
    var lastHasFocusHolder = -1

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val song: Song = songs[position]
        holder.binding.num = position.toString()
        holder.binding.song = songs[position]
        /*holder.idView.text = position.toString() + "   " + song.name
        holder.contentView.text =
            song.artist.get()!!.name.get()!! + " - " + song.album.get()!!.title.get()!!

        GlobalScope.launch(Dispatchers.Main) {
            Glide.with(holder.itemView.context)
                .load(MediaStoreProvider.getArt(song))
                .placeholder(R.drawable.unknown)
                .error(R.drawable.unknown)
                .transform(CenterCrop(), RoundedCorners(4))
                .into(holder.imageView)
        }*/
    }

    override fun getItemCount(): Int = songs.size

    interface OnClick {
        fun onClick(position: Int, holder: ViewHolder)
    }

    inner class ViewHolder(val binding: MusicItemFragmentBinding) :
        RecyclerView.ViewHolder(binding.root)

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