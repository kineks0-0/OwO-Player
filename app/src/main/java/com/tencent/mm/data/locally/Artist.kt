package com.tencent.mm.data.locally

import androidx.databinding.ObservableField
import com.tencent.mm.data.locally.utils.MediaStoreProvider


data class Artist (
    var id  : ObservableField<Int> = ObservableField(-1),
    var name: ObservableField<String>   = ObservableField(MediaStoreProvider.UNKNOWN),
)