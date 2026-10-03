package com.skkaushal.promediaplayer.ui

import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skkaushal.promediaplayer.R

class FolderDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private val videoList = ArrayList<VideoModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_detail)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvFolderTitle = findViewById<TextView>(R.id.tvFolderTitle)
        recyclerView = findViewById(R.id.rvFolderVideos)

        val folderName = intent.getStringExtra("FOLDER_NAME") ?: "Videos"
        tvFolderTitle.text = folderName

        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        videoAdapter = VideoAdapter(this, videoList)
        recyclerView.adapter = videoAdapter

        loadFolderVideos(folderName)
    }

    private fun loadFolderVideos(folderName: String) {
        videoList.clear()

        // DISPLAY_NAME is the real file name. TITLE is media metadata and can be
        // different from the filename (and is often empty or provider-generated).
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Video.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(folderName)

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = cursor.getString(nameCol).orEmpty()
                    .ifBlank { "Video_${id}" }
                val duration = cursor.getLong(durationCol)
                val size = cursor.getLong(sizeCol)

                val uri = android.content.ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                videoList.add(
                    VideoModel(
                        id = id,
                        title = displayName,
                        path = "",
                        duration = duration,
                        size = size,
                        uri = uri
                    )
                )
            }
        }

        videoAdapter.notifyDataSetChanged()
    }
}
