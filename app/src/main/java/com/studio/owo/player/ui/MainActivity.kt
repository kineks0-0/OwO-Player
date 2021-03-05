package com.studio.owo.player.ui


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.studio.owo.player.OwOPlayerApplication
import com.studio.owo.player.getContext
import com.studio.owo.player.ui.MainActivity.BackHandlerHelper.handleBackPress
import com.studio.owo.player.ui.MainActivity.BackHandlerHelper.isFragmentBackHandled
import com.studio.owo.player.ui.viewpage.*
import com.studio.owo.player.ui.viewpage.model.PlayingViewModel
import com.tencent.mm.R
import com.tencent.mm.databinding.ActivityMainBinding
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val debugMode: Boolean = false

    lateinit var pagerAdapter: PagerAdapter
    private lateinit var playingViewModel: PlayingViewModel
    private var title = getContext().getString(R.string.app_name)
    set(newTitle){
        binding.topAppBar.title = newTitle
        field = newTitle
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // init ViewModel
        playingViewModel = ViewModelProvider(this).get(PlayingViewModel::class.java)
        playingViewModel.onCreate(this)

        // init NavView & setListener
        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> binding.viewPage.currentItem = 0
                R.id.navigation_albums -> binding.viewPage.currentItem = 1
                R.id.navigation_playing -> binding.viewPage.currentItem = 2
                R.id.navigation_setting -> binding.viewPage.currentItem = 3
            }
            true
        }

        // init ViewPager Adapter
        pagerAdapter = PagerAdapter(supportFragmentManager)
        binding.viewPage.adapter = pagerAdapter

        // init ViewPager onPageChange Listener
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
                title = pagerAdapter.getPageTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }

        //binding.viewPage.setPageTransformer(DepthPageTransformer())
        binding.viewPage.addOnPageChangeListener(onPageChangeCallback)
        binding.viewPage.offscreenPageLimit = 4
        onPageChangeCallback.onPageSelected(binding.viewPage.currentItem)



    }

    override fun onStart() {
        super.onStart()
        playingViewModel.onStart(this)

        if (debugMode)
            findFocusView()
    }

    // 寻找焦点View,用于焦点丢失时调试
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


    override fun onResume() {
        super.onResume()
        // 判断是否需要运行时申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            // 判断是否需要对用户进行提醒，用户点击过拒绝&&没有勾选不再提醒时进行提示
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )) {
                // 给用于予以权限解释, 对于已经拒绝过的情况，先提示申请理由，再进行申请
                Snackbar.make(binding.root, "程序需要读写权限来读取缓存和导出歌曲", Snackbar.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    OwOPlayerApplication.REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE
                )
            } else {
                // 无需说明理由的情况下，直接进行申请。如第一次使用该功能（第一次申请权限），用户拒绝权限并勾选了不再提醒
                // 将引导跳转设置操作放在请求结果回调中处理
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    OwOPlayerApplication.REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            OwOPlayerApplication.REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE -> {
                // 判断用户是否同意了请求
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: 刷新数据
                } else {
                    // 未同意的情况
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        // 给用于予以权限解释, 对于已经拒绝过的情况，先提示申请理由，再进行申请
                        Snackbar.make(binding.root, "程序需要读写权限播放歌曲", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playingViewModel.onDestroy(this)
    }

    fun onBackKey(): Boolean {
        return isFragmentBackHandled(pagerAdapter.getItem(binding.viewPage.currentItem))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = playingViewModel.onKeyDown(
        keyCode,
        event,
        this
    )



    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        private val musicFragment: MusicFragment = MusicFragment.newInstance(1)
        private val albumFragment: AlbumFragment = AlbumFragment.newInstance(2)
        private val playingFragment: PlayingFragment = PlayingFragment.newInstance()
        private val settingFragment: SettingsFragment = SettingsFragment()
        val fragments: ArrayList<Fragment> = ArrayList()

        init {
            fragments.add(musicFragment)
            fragments.add(albumFragment)
            fragments.add(playingFragment)
            fragments.add(settingFragment)

            // 播放界面的标题设置 liveData
            albumFragment.title
                .observe(this@MainActivity, { newTitle -> setTitle(newTitle,1)})
            // 播放界面的标题设置 liveData
            playingFragment.title
                .observe(this@MainActivity, { newTitle -> setTitle(newTitle,2)})
        }

        fun setTitle(newTitle: String, index: Int) {
            if (binding.viewPage.currentItem == index) {
                title = newTitle
            }
        }

        override fun getCount(): Int = fragments.size

        override fun getItem(i: Int): Fragment {
            return fragments[i]
        }

        override fun getPageTitle(position: Int): String {
            return when (fragments[position]) {
                musicFragment -> MusicFragment.title
                albumFragment -> albumFragment.title.value
                playingFragment -> playingFragment.title.value
                settingFragment -> SettingsFragment.title
                else -> null
            } ?: getString(R.string.app_name)
        }

    }

    object BackHandlerHelper {
        /**
         * 将back事件分发给 FragmentManager 中管理的子Fragment，如果该 FragmentManager 中的所有Fragment都
         * 没有处理back事件，则尝试 FragmentManager.popBackStack()
         *
         * @return 如果处理了back键则返回 **true**
         * @see .handleBackPress
         * @see .handleBackPress
         */
        fun handleBackPress(fragmentManager: MainActivity.PagerAdapter): Boolean {
            val fragments: List<Fragment> = fragmentManager.fragments
            for (i in fragments.indices.reversed()) {
                val child: Fragment = fragments[i]
                if (isFragmentBackHandled(child)) {
                    return true
                }
            }
            return false
        }

        /**
         * 判断Fragment是否处理了Back键
         *
         * @return 如果处理了back键则返回 **true**
         */
        fun isFragmentBackHandled(fragment: Fragment?): Boolean {
            return (fragment != null && fragment.isVisible
                    //&& fragment.getUserVisibleHint() //for ViewPager
                    && fragment is FragmentBackHandler
                    && (fragment as FragmentBackHandler).onBackPressed())
        }
    }

}