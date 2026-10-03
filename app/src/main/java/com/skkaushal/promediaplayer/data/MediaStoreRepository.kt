package com.skkaushal.promediaplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.skkaushal.promediaplayer.model.FolderModel
import com.skkaushal.promediaplayer.model.MediaItemModel

class MediaStoreRepository(private val context: Context) {

    fun fetchFoldersWithMedia(): List<FolderModel> {
        val folderMap = mutableMapOf<String, MutableList<MediaItemModel>>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: "Video File"
                val duration = cursor.getLong(durationCol)
                val mime = cursor.getString(mimeCol) ?: "video/*"
                val folderName = cursor.getString(bucketCol) ?: "Internal Storage"
                val size = cursor.getLong(sizeCol)
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)

                val uriStr = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                ).toString()

                val item = MediaItemModel(
                    id = id,
                    title = name,
                    uri = uriStr,
                    duration = duration,
                    isVideo = true,
                    mime = mime,
                    size = size,
                    width = width,
                    height = height
                )

                if (!folderMap.containsKey(folderName)) {
                    folderMap[folderName] = mutableListOf()
                }
                folderMap[folderName]?.add(item)
            }
        }

        return folderMap.map { (folderName, items) ->
            FolderModel(id = folderName, name = folderName, mediaList = items)
        }
    }
}
