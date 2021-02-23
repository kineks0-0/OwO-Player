package com.tencent.mm.ui


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.tencent.mm.R
import com.tencent.mm.databinding.ActivityMainBinding
import com.studio.owo.player.ui.viewpage.*
import com.studio.owo.player.ui.viewpage.model.PlayingViewModel
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val debugMode: Boolean = false

    lateinit var pagerAdapter: PagerAdapter
    private lateinit var playingViewModel: PlayingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playingViewModel = ViewModelProvider(this).get(PlayingViewModel::class.java)
        playingViewModel.onCreate(this)

        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> binding.viewPage.currentItem = 0
                R.id.navigation_albums -> binding.viewPage.currentItem = 1
                R.id.navigation_playing -> binding.viewPage.currentItem = 2
                R.id.navigation_setting -> binding.viewPage.currentItem = 3
            }
            true
        }

        pagerAdapter = PagerAdapter(supportFragmentManager)
        binding.viewPage.adapter = pagerAdapter


        val onPageChangeCallback = object : ViewPager.OnPageChangeListener {
            var lastPosition = 0
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                (pagerAdapter.getItem(lastPosition) as OnPageSelectedChange).onPageSelectedChange(
                    false,
                    position
                )
                (pagerAdapter.getItem(position) as OnPageSelectedChange).onPageSelectedChange(
                    true,
                    position
                )
                lastPosition = position
                binding.navView.menu.getItem(position).isChecked = true
                supportActionBar?.title = pagerAdapter.getPageTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }

        //binding.viewPage.setPageTransformer(DepthPageTransformer())
        binding.viewPage.addOnPageChangeListener(onPageChangeCallback)
        binding.viewPage.offscreenPageLimit = 4
        onPageChangeCallback.onPageSelected(binding.viewPage.currentItem)

        val playingFragmentTitleObserve = Observer<String> { newTitle ->
            //Log.d(this::class.java.toString(),"Title update")
            if (binding.viewPage.currentItem == 2) {
                this.supportActionBar?.let {
                    it.title = newTitle
                } ?: run { title = newTitle }
            }
        }

        PlayingViewModel.playingFragmentTitle.observe(this, playingFragmentTitleObserve)

    }


    override fun onStart() {
        super.onStart()
        playingViewModel.onStart(this)

        if (debugMode)
            findFocusView()
    }

    private fun findFocusView() {
        var focusedChild: View?
        thread {
            repeat(1000) {
                Thread.sleep(500)
                binding.root.post {
                    focusedChild = binding.viewPage.rootView.findFocus()
                    focusedChild?.setBackgroundColor(Color.YELLOW)
                    focusedChild?.onFocusChangeListener ?: let {
                        focusedChild?.setOnFocusChangeListener { v, hasFocus ->
                            if (!hasFocus) {
                                v.setBackgroundColor(Color.parseColor("#00000000"))
                            }
                        }
                    }
                    if (focusedChild != null) {
                        Log.w(this.javaClass.name, "binding.root.focusedChild = $focusedChild")
                    } else {

                        Log.w(this.javaClass.name, "binding.root.focusedChild = null")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playingViewModel.onDestroy(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return playingViewModel.onKeyDown(keyCode,event,this)
    }


    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        private val musicFragment: MusicFragment = MusicFragment.newInstance(1)
        private val albumFragment: AlbumFragment = AlbumFragment.newInstance(2)
        private val playingFragment: PlayingFragment = PlayingFragment.newInstance()
        private val settingFragment: SettingFragment = SettingFragment.newInstance(1)

        override fun getCount(): Int = 4

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> musicFragment//MusicFragment.newInstance(1)
                1 -> albumFragment//AlbumFragment.newInstance(2)
                2 -> playingFragment//PlayingFragment.newInstance()
                3 -> settingFragment//SettingFragment.newInstance(1)
                else -> Fragment()
            }
        }

        /*fun getItem(f: Fragment) : Int {
            return when(f) {
                musicFragment -> 0
                albumFragment -> 1
                playingFragment -> 2
                settingFragment -> 3
                else -> -1
            }
        }*/

        override fun getPageTitle(position: Int): String {
            return when (position) {
                0 -> MusicFragment.title
                1 -> AlbumFragment.title
                2 -> PlayingViewModel.playingFragmentTitle.value
                3 -> SettingFragment.title
                else -> "OBJECT ${(position + 1)}"
            } ?: getString(R.string.app_name)
        }

        /*fun setPageTitle(position: Int, string: String) {
            //return "OBJECT ${(position + 1)}"
            when (position) {
                0 -> MusicFragment.title = string
                1 -> AlbumFragment.title = string
                2 -> playingViewModel.playingFragmentTitle.value = string
                3 -> SettingFragment.title = string
            }
        }*/

    }

}