package com.tencent.mm.data.locally

import androidx.databinding.ObservableField

data class Album (
    //var album    : String = MediaStoreProvider.UNKNOWN,
    var art      : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN_ART),
    var id       : ObservableField<Int>    = ObservableField(-1),
    var size     : ObservableField<Int>    = ObservableField(-1),
    var title    : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN),
    var artist   : ObservableField<Artist> = ObservableField(Artist()),

    )