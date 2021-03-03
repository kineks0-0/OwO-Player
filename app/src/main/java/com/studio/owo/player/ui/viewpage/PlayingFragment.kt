package com.studio.owo.player.ui.viewpage

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.tencent.mm.R
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.data.locally.utils.MusicPlay
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.getContext
import com.tencent.mm.databinding.PlayingFragmentBinding
import com.studio.owo.player.ui.viewpage.model.PlayingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class PlayingFragment : Fragment(), OnPageSelectedChange {

    companion object {
        private const val rdp = 10              //图片圆角值
        fun newInstance() = PlayingFragment()   //外部实例化

        @BindingAdapter("android:song") //layout中dataBinding接口
        @JvmStatic
        fun setSongImage(imageView: ImageView, song: Song?) {
            // 仅 View 可见 和 LoadArt 为真时加载
            if (imageView.visibility == View.VISIBLE && MediaStoreProvider.loadAlbumArt) {

                GlobalScope.launch(Dispatchers.Main) {
                    Glide.with(imageView.context)
                        .load(
                            song.let {
                                if (it != null)
                                    return@let MediaStoreProvider.getArtUri(it)
                                else
                                    return@let null//MediaStoreProvider.UNKNOWN_ART_RES
                            }
                        )
                        .placeholder(imageView.background)
                        .error(imageView.background)
                        //.error(R.drawable.view_background)
                        .transform(CenterCrop(), RoundedCorners(rdp))
                        .into(imageView)
                }
            }
        }

    }


    val title: MutableLiveData<String> by lazy {
        MutableLiveData(
            com.studio.owo.player.getContext().resources.getString(
                R.string.app_name
            )
        )
    }
    private lateinit var binding: PlayingFragmentBinding
    private lateinit var viewModel: PlayingViewModel
    // 设置播放监听,在onDestroyView时移除监听
    private val onPlayListener = MusicPlay.addOnPlayListener(object : MusicPlay.OnPlayListener {

        @SuppressLint("SetTextI18n")
        override fun onPlayBegins(song: Song, songList: ArrayList<Song>, index: Int) =
            onPlayButtonRedraw()

        override fun onPlayStop() = onPlayButtonRedraw()
        override fun onPlayEnd() = onPlayButtonRedraw()
        override fun onPlayPause() = onPlayButtonRedraw()
        override fun onPlayContinues() = onPlayButtonRedraw()
        override fun onRest() = onViewRedraw()
        override fun onError() = onPlayButtonRedraw()

        override fun onPlayModeChange(playModeType: Int) {
            if (this@PlayingFragment.isVisible)
                viewModel.onPlayModeChange(this@PlayingFragment.requireView(), playModeType)
        }

        @SuppressLint("SetTextI18n")
        override fun onViewRedraw() {
            //仅 Fragment 可见时更新视图
            if (!this@PlayingFragment.isVisible) return
            binding.playModel = viewModel
            onPlayButtonRedraw()
            title.value = viewModel.song.get()?.name?.get()
        }

        fun onPlayButtonRedraw() {
            //仅 Fragment 可见时更新视图
            if (!this@PlayingFragment.isVisible) return
            //binding.playing = MusicPlay.isPlaying
            if (MusicPlay.isPlaying)
                binding.playButton.setImageResource(R.drawable.ic_pause_black_48dp)
            else
                binding.playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp)
        }

    })



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlayingFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        //播放专辑图设置焦点效果(按键兼容)
        binding.playingImageView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                thread {
                    var loop = true
                    while (loop) {
                        loop = v.hasFocus()
                        if (loop) {
                            v.post {
                                v.alpha = 0.8f
                            }
                            Thread.sleep(250)
                            v.post {
                                v.alpha = 1f
                            }
                            Thread.sleep(2000)
                        }
                    }
                }
            } else {
                v.alpha = 1f
            }
        }

        // 专辑图设置兼容阴影 (Api16-20兼容)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            binding.playingImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            binding.nextPlayImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            binding.nextPlayImageView2.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlayingViewModel::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
        MusicPlay.removePlayListener(onPlayListener)
    }

    override fun onPageSelectedChange(hasFocus: Boolean, position: Int) {
        // post 来避免事件回调获取焦点时 view 尚未准备
        Handler(Looper.getMainLooper()).post {
            if (!this@PlayingFragment.isVisible) return@post
            if (hasFocus) {
                binding.playButton.requestFocus()
                binding.playingImageView.isFocusable = true
            } else {
                binding.playingImageView.isFocusable = false
            }
        }
    }

}