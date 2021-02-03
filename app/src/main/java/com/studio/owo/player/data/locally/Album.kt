package com.studio.owo.player.data.locally

import androidx.databinding.ObservableField
import com.studio.owo.player.data.locally.utils.MediaStoreProvider

data class Album (
    //var album    : String = MediaStoreProvider.UNKNOWN,
    var art      : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN_ART),
    var id       : ObservableField<Int>    = ObservableField(-1),
    var size     : ObservableField<Int>    = ObservableField(-1),
    var title    : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN),
    var artist   : ObservableField<Artist> = ObservableField(Artist()),

    )