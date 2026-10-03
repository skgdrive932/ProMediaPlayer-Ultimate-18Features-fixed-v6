package com.skkaushal.promediaplayer.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skkaushal.promediaplayer.R
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class VideoAdapter(
    private val context: Context,
    private val videoList: List<VideoModel>
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val thumbnailExecutor = Executors.newFixedThreadPool(3)

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgVideoThumbnail)
        val tvTitle: TextView = itemView.findViewById(R.id.tvVideoTitle)
        val tvDuration: TextView = itemView.findViewById(R.id.tvVideoDuration)
        val tvSize: TextView = itemView.findViewById(R.id.tvVideoSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]

        // Always show the actual MediaStore filename, not media metadata/title.
        holder.tvTitle.text = video.title
        holder.tvDuration.text = formatDuration(video.duration)
        holder.tvSize.text = formatSize(video.size)

        // Reset recycled image immediately.
        holder.imgThumbnail.setImageResource(android.R.drawable.ic_media_play)
        holder.imgThumbnail.tag = video.uri.toString()

        val uri = video.uri
        val expectedTag = uri.toString()

        thumbnailExecutor.execute {
            val bitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(
                        uri,
                        Size(320, 180),
                        null
                    )
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver,
                        video.id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    )
                }
            } catch (_: Exception) {
                null
            }

            holder.imgThumbnail.post {
                // Avoid showing a thumbnail for a recycled row.
                if (holder.imgThumbnail.tag == expectedTag && bitmap != null) {
                    holder.imgThumbnail.setImageBitmap(bitmap)
                }
            }
        }

        // Open OUR Media3 player, not an external Android video app.
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                // Pass the MediaStore URI in both Intent.data and an explicit extra.
                // Some Android/device combinations can lose data while launching an
                // explicit Activity, so PlayerActivity has a reliable fallback.
                data = video.uri
                type = "video/*"
                putExtra("video_uri", video.uri.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = videoList.size

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun formatSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 MB"
        val mb = sizeBytes.toDouble() / (1024 * 1024)
        val gb = mb / 1024
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", sizeBytes / 1024.0)
        }
    }
}
