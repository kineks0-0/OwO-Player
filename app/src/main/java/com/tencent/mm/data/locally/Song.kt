package com.tencent.mm.data.locally

import androidx.databinding.ObservableField
import java.io.File

data class Song(
    var file     : ObservableField<File>,
    val id       : ObservableField<Int> = ObservableField(-1),
    var name     : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN),
    var displayName: ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN),
    var duration : ObservableField<Int> = ObservableField(-1),
    var artist   : ObservableField<Artist> = ObservableField(Artist()),
    var album    : ObservableField<Album> = ObservableField(Album()),
    var year     : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN),
    var type     : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN),
    var size     : ObservableField<String> = ObservableField(MediaStoreProvider.UNKNOWN)) {

    override fun equals(other: Any?): Boolean {
        return file.get() == other
    }

    override fun hashCode(): Int {
        return file.get().hashCode()
    }

}