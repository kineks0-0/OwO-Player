package com.tencent.mm.data.locally

import com.tencent.mm.OwOPlayerApplication
import com.tencent.mm.getContext
import java.io.File

object FileProvider {

    private val CacheFolder: File = getContext().externalCacheDir ?: OwOPlayerApplication.context.cacheDir
    private val CacheNetFolder: File
    private val CacheArtFolder: File
    private val CacheOtherFolder: File


    init {
        CacheFolder.mkdirs()
        CacheNetFolder = File(CacheFolder.absolutePath + "/NetData/")
        CacheNetFolder.mkdirs()
        CacheArtFolder = File(CacheFolder.absolutePath + "/DecodeData/")
        CacheArtFolder.mkdirs()
        CacheOtherFolder = File(CacheFolder.absolutePath + "/Other/")
        CacheOtherFolder.mkdirs()
    }


    fun getNetCacheFile(fileName: String): File {
        return File(CacheNetFolder, fileName)
    }

    fun getArtCacheFile(fileName: String): File {
        return File(CacheArtFolder, fileName)
    }

    fun getOtherCacheFile(fileName: String): File {
        return File(CacheOtherFolder, fileName)
    }
}