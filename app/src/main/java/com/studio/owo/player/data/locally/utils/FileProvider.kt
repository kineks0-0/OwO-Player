package com.studio.owo.player.data.locally.utils

import com.studio.owo.player.OwOPlayerApplication
import com.studio.owo.player.getContext
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