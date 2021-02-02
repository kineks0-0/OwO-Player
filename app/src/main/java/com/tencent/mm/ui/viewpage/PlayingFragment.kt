package com.tencent.mm.ui.viewpage

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.FocusFinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.snackbar.Snackbar
import com.tencent.mm.R
import com.tencent.mm.data.locally.MediaStoreProvider
import com.tencent.mm.data.locally.MusicPlay
import com.tencent.mm.data.locally.Song
import com.tencent.mm.databinding.PlayingFragmentBinding
import com.tencent.mm.getContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.concurrent.thread

class PlayingFragment : Fragment(), OnPageSelectedChange {

    companion object {
        private const val rdp = 10
        fun newInstance() = PlayingFragment()

        @BindingAdapter("android:song")
        @JvmStatic
        fun setSongImage(imageView: ImageView, song: Song?) {
            if (imageView.visibility == View.VISIBLE) {
                GlobalScope.launch(Dispatchers.Main) {
                    Glide.with(imageView.context)
                        .load(
                            song.let {
                                if (it != null)
                                    return@let MediaStoreProvider.getArtByteArray(it)
                                else
                                    return@let MediaStoreProvider.UNKNOWN_ART_RES
                            }
                        )
                        .placeholder(R.drawable.view_background)
                        .error(R.drawable.view_background)
                        .transform(CenterCrop(), RoundedCorners(rdp))
                        .into(imageView)
                }
            }
        }

    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var _binding: PlayingFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlayingViewModel

    private val requestManager: RequestManager by lazy {
        Glide.with(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.root.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                thread {
                    var loop = true
                    while (loop) {
                        loop = v.hasFocus()
                        if (loop) {
                            v.post {
                                v.alpha = 0.5f
                            }
                            Thread.sleep(500)
                            v.post {
                                v.alpha = 1f
                            }
                            Thread.sleep(900)
                        }
                    }
                }
            } else {
                v.alpha = 1f
            }
        }

        binding.playingImageView.onFocusChangeListener = binding.root.onFocusChangeListener
        binding.nextPlayImageView.onFocusChangeListener = binding.root.onFocusChangeListener
        binding.nextPlayImageView2.onFocusChangeListener = binding.root.onFocusChangeListener

        MusicPlay.onPlayListener = object : MusicPlay.OnPlayListener {
            @SuppressLint("SetTextI18n")
            override fun onPlayBegins
                        (song: Song, songList: ArrayList<Song>, index: Int) = onPlayButtonRedraw()

            override fun onPlayStop() = onPlayButtonRedraw()
            override fun onPlayEnd() = onPlayButtonRedraw()
            override fun onPlayPause() = onPlayButtonRedraw()
            override fun onPlayContinues() = onPlayButtonRedraw()
            override fun onRest() {
                onViewRedraw()
            }
            override fun onError() = onPlayButtonRedraw()

            override fun onPlayModeChange(playModeType: Int) =
                viewModel.onPlayModeChange(this@PlayingFragment.requireView(), playModeType)


            @SuppressLint("SetTextI18n")
            override fun onViewRedraw() {
                binding.playModel = viewModel
                viewModel.playingFragmentTitle.value = binding.songTitleTextView.text as String
                onPlayButtonRedraw()
            }

            fun onPlayButtonRedraw() {
                //binding.playing = MusicPlay.isPlaying
                if (MusicPlay.isPlaying)
                    binding.playImageView.setImageResource(R.drawable.ic_pause_black_48dp)
                else
                    binding.playImageView.setImageResource(R.drawable.ic_play_arrow_black_48dp)
            }

        }

        MusicPlay.onPlayListener.onViewRedraw()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlayingViewModel::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPageSelectedChange(hasFocus: Boolean, position: Int) {

    }

}