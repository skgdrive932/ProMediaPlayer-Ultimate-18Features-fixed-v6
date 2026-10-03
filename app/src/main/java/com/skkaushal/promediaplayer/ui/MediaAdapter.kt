package com.skkaushal.promediaplayer.ui

import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skkaushal.promediaplayer.databinding.ItemMediaBinding
import com.skkaushal.promediaplayer.model.MediaItemModel

class MediaAdapter(
    private val mediaList: List<MediaItemModel>,
    private val onMediaClick: (MediaItemModel) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        val context = holder.itemView.context

        holder.binding.tvTitle.text = media.title
        holder.binding.tvDuration.text = formatDuration(media.duration)
        holder.binding.tvSize.text = formatFileSize(media.size)

        // Resolution & FPS badge format (e.g., 480p@23.98 or 720p@24)
        val resHeight = if (media.height > 0) media.height else 480
        holder.binding.tvResolution.text = "${resHeight}p@23.98"

        // Thumbnail Extraction
        try {
            val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, media.id)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bitmap: Bitmap = context.contentResolver.loadThumbnail(contentUri, Size(150, 100), null)
                holder.binding.imgThumbnail.setImageBitmap(bitmap)
            } else {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver, media.id, MediaStore.Video.Thumbnails.MINI_KIND, null
                )
                holder.binding.imgThumbnail.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            holder.binding.imgThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onMediaClick(media)
        }
    }

    override fun getItemCount(): Int = mediaList.size

    private fun formatDuration(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = ms / (1000 * 60 * 60)
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 MB"
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.0f MB", mb)
            else -> String.format("%.0f KB", kb)
        }
    }
}
