package com.studio.owo.player.ui.viewpage

import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.google.android.material.snackbar.Snackbar
import com.studio.owo.player.data.locally.utils.MediaStoreProvider
import com.studio.owo.player.getContext
import com.tencent.mm.R
import kotlin.concurrent.thread

class SettingsFragment : PreferenceFragmentCompat(), OnPageSelectedChange {

    companion object {
        var title = getContext().getString(R.string.title_setting)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("ignoreCacheAlbumArt")
            ?.setOnPreferenceChangeListener { _, newValue ->
                MediaStoreProvider.ignoreCacheAlbumArt = (newValue as Boolean)
                Snackbar.make(this.requireView(), R.string.take_effect_after_restart, Snackbar.LENGTH_SHORT)
                    .show()
                true
            }

        findPreference<SwitchPreferenceCompat>("load_album_art")
            ?.setOnPreferenceChangeListener { _, newValue ->
                MediaStoreProvider.loadAlbumArt = (newValue as Boolean)
                Snackbar.make(this.requireView(), R.string.take_effect_after_restart, Snackbar.LENGTH_SHORT)
                    .show()
                true
            }

        findPreference<Preference>("update_media_store")
            ?.setOnPreferenceClickListener { _ ->
                Snackbar.make(this.requireView(), R.string.not_yet_supported, Snackbar.LENGTH_SHORT)
                    .show()
                true
            }

        findPreference<Preference>("clear_cache")
            ?.setOnPreferenceClickListener { _ ->
                requireView().post {
                    Glide.get(com.studio.owo.player.getContext()).clearMemory()
                    Glide.get(com.studio.owo.player.getContext()).setMemoryCategory(MemoryCategory.LOW)
                }
                thread {
                    Glide.get(com.studio.owo.player.getContext()).clearDiskCache()
                }
                Snackbar.make(this.requireView(), R.string.cleaned_up, Snackbar.LENGTH_SHORT)
                    .show()
                true
            }


    }

    override fun onStart() {
        super.onStart()
        if (super.getListView()==null || super.getListView()?.adapter == null) return

        for (i in 0 until 0+super.getListView().childCount) {
            super.getListView().findViewHolderForAdapterPosition(i)?.
            itemView?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    hasFocusHolder = i
                } else {
                    notFocusHolder = i
                }
            }
        }
        super.getListView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //if (newState!=RecyclerView.SCROLL_STATE_IDLE) return
                if (recyclerView.childCount > 0) {
                    try {
                        val currentPosition = (recyclerView.getChildAt(0)
                            .layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
                        Log.d("=====currentPosition", currentPosition.toString())
                        /*notFocusHolder = lastHasFocusHolder
                        lastHasFocusHolder = currentPosition + (recyclerView.childCount / 2) + 1*/
                        for (i in currentPosition until currentPosition+recyclerView.childCount) {
                            recyclerView.findViewHolderForAdapterPosition(i)?.
                            itemView?.apply {
                                if (onFocusChangeListener==null) {
                                    setOnFocusChangeListener { _, hasFocus ->
                                        if (hasFocus) {
                                            hasFocusHolder = i
                                        } else {
                                            notFocusHolder = i
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("=====currentPosition", e.message, e)
                    }
                }
            }
        })
    }

    private var notFocusHolder = -1
    private var lastHasFocusHolder = -1
    private var hasFocusHolder = -1
    override fun onPageSelectedChange(hasFocus: Boolean, position: Int) {
        with(super.getListView()) {
            if (adapter == null) {
                return@with
            }
            //val adapter = (adapter as AlbumItemRecyclerViewAdapter)
            if (hasFocus) {
                findViewHolderForAdapterPosition(lastHasFocusHolder)?.itemView?.let {
                    it.requestFocus()
                    it.clearFocus()
                    it.requestFocus()
                    thread {
                        Thread.sleep(20)
                        post {
                            it.clearFocus()
                            it.requestFocus()
                        }
                    }
                }
            } else {
                lastHasFocusHolder = notFocusHolder
            }
        }

    }

}