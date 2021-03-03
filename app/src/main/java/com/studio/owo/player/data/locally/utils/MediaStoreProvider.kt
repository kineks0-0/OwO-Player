package com.studio.owo.player.data.locally.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import androidx.databinding.ObservableField
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.tencent.mm.R
import com.studio.owo.player.data.locally.Album
import com.studio.owo.player.data.locally.Artist
import com.studio.owo.player.data.locally.Song
import com.studio.owo.player.getContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


object MediaStoreProvider {

    val UNKNOWN = getContext().getString(R.string.unknown)
    const val UNKNOWN_ART_RES = R.drawable.unknown
    val UNKNOWN_ART = "android.resource://" +
            getContext().packageName +
            "/drawable/$UNKNOWN_ART_RES"

    var ignoreCacheAlbumArt = PreferenceManager.getDefaultSharedPreferences(getContext())
        .getBoolean("ignoreCacheAlbumArt", false)
    var loadAlbumArt = PreferenceManager.getDefaultSharedPreferences(getContext())
        .getBoolean("load_album_art", true)
        set(value) {
            if (!value) {
                Handler(Looper.getMainLooper()).post {
                    Glide.get(getContext()).clearMemory()
                    thread {
                        Glide.get(getContext()).clearDiskCache()
                    }
                    Glide.get(getContext()).setMemoryCategory(MemoryCategory.LOW)
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    Glide.get(getContext()).setMemoryCategory(MemoryCategory.NORMAL)
                }
            }
            field = value
        }

    fun getArtUri(song: Song): Uri {
        return getArtCache(song)
    }

    fun getArt(album: Album): String? {
        return getArtCache(album.id.get()!!)
    }

    private fun getArtCache(song: Song): Uri {
        return Uri.parse("content://media/external/audio/media/${song.id.get()}/albumart")
    }

    private fun getArtCache(albumID: Int): String? {
        val mUriAlbums = "content://media/external/audio/albums"
        val projection = arrayOf("album_art")
        var albumArt: String? = null
        if (albumID != -1) {
            val cur: Cursor? = getContext()
                .contentResolver.query(
                    Uri.parse("$mUriAlbums/$albumID"),
                    projection,
                    null,
                    null,
                    null
                )
            if (cur != null && cur.count > 0 && cur.columnCount > 0) {
                cur.moveToNext()
                albumArt = cur.getString(0)
                cur.close()
            }
        }
        return albumArt
    }


    private val BASE_PROJECTION = arrayOf(
        BaseColumns._ID,   //0
        AudioColumns.DISPLAY_NAME,   //1
        AudioColumns.TITLE,   //2
        AudioColumns.DURATION,   //3
        AudioColumns.ARTIST,   //4
        AudioColumns.ARTIST_ID,   //5
        AudioColumns.ALBUM,   //9
        AudioColumns.ALBUM_ID,   //10
        AudioColumns.ALBUM_ARTIST,   //13
        AudioColumns.YEAR,   //14
        AudioColumns.SIZE,   //17
        AudioColumns.DATA,   //18
        AudioColumns.TRACK,   //19
        AudioColumns.MIME_TYPE,   //20
    )
    private const val ID = 0
    private const val DISPLAY_NAME = 1
    private const val TITLE = 2
    private const val DURATION = 3
    private const val ARTIST = 4
    private const val ARTIST_ID = 5

    private const val ALBUM = 6
    private const val ALBUM_ID = 7

    private const val ALBUM_ARTIST = 8
    private const val YEAR = 9

    private const val SIZE = 10
    private const val DATA = 11
    private const val TRACK = 12
    private const val MIME_TYPE = 13


    @SuppressLint("Recycle")
    suspend fun querySongs(): ArrayList<Song> {
        return withContext(Dispatchers.Default) {
            val cursor: Cursor = getContext()
                .contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, BASE_PROJECTION,
                    "is_music != 0", null, null
                ) ?: return@withContext ArrayList()
            return@withContext loadSongs(cursor)
        }
    }

    @SuppressLint("Recycle")
    suspend fun querySongs(album: Album): ArrayList<Song> {
        val albumId = album.id.get()
        return withContext(Dispatchers.Default) {
            val cursor: Cursor = getContext()
                .contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, BASE_PROJECTION,
                    "is_music != 0 and album_id = $albumId", null, null
                ) ?: return@withContext ArrayList()
            return@withContext loadSongs(cursor)
        }
    }

    @SuppressLint("Recycle")
    suspend fun querySongsFromAlbums(albumID: Int): ArrayList<Song> {
        return withContext(Dispatchers.Default) {
            val cursor: Cursor = getContext()
                .contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, BASE_PROJECTION,
                    "is_music != 0 and album_id = $albumID", null, null
                ) ?: return@withContext ArrayList()

            return@withContext loadSongs(cursor)
        }

    }

    private suspend fun loadSongs(cursor: Cursor): ArrayList<Song> {

        return withContext(Dispatchers.Default) {
            val songs: ArrayList<Song> = ArrayList()

            if (cursor.moveToFirst()) {
                var song: Song
                do {
                    cursor.let {
                        val artist = Artist(
                            ObservableField(it.getInt(ARTIST_ID)),
                            ObservableField(it.getString(ARTIST))
                        )
                        song = Song(
                            ObservableField(File(it.getString(DATA))),// 文件路径
                            ObservableField(it.getInt(ID)),// id
                            ObservableField(it.getString(TITLE)),// 歌曲名
                            ObservableField(it.getString(DISPLAY_NAME)),// 文件名
                            ObservableField(it.getInt(DURATION)),// 时长
                            ObservableField(artist),// 歌手
                            ObservableField(
                                Album(
                                    ObservableField(""),
                                    ObservableField(it.getInt(ARTIST_ID)),
                                    ObservableField(-1),
                                    ObservableField(it.getString(ALBUM)),
                                    ObservableField(artist)
                                )
                            ),// 专辑
                            ObservableField(it.getString(YEAR) ?: UNKNOWN),// 年代
                            ObservableField(it.getString(MIME_TYPE).trim()),// 格式
                            ObservableField(it.getString(SIZE)),// 文件大小
                        )
                    }
                    songs.add(song)
                } while (cursor.moveToNext())
                cursor.close()
            }
            return@withContext songs
        }
    }

    @SuppressLint("Recycle")
    suspend fun queryAlbum(): ArrayList<Album> {
        return withContext(Dispatchers.Default) {
            val resolver: ContentResolver = getContext().contentResolver
            val dataColumns: Array<String> = arrayOf(
                MediaStore.Audio.Media._ID,                //0
                MediaStore.Audio.Albums.ALBUM,              //1
                MediaStore.Audio.Albums.ARTIST,             //2
                MediaStore.Audio.Media.ARTIST_ID,          //3
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,    //4
            )
            val cursor = resolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                dataColumns,
                null,
                null,
                null
            )

            val albums: ArrayList<Album> = ArrayList()

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    var album: Album
                    do {
                        cursor.let {
                            album = Album(
                                ObservableField("art"), ObservableField(it.getInt(0)),
                                ObservableField(it.getInt(4)), ObservableField(it.getString(1)),
                                ObservableField(
                                    Artist(
                                        ObservableField(it.getInt(3)),
                                        ObservableField(it.getString(2))
                                    )
                                )
                            )
                        }
                        albums.add(album)
                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }
            return@withContext albums
        }
    }


}