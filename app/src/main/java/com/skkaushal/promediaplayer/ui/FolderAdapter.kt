package com.skkaushal.promediaplayer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skkaushal.promediaplayer.R
import com.skkaushal.promediaplayer.databinding.ItemFolderBinding
import com.skkaushal.promediaplayer.model.FolderModel
import java.util.Locale

class FolderAdapter(
    private val folderList: List<FolderModel>,
    private val onFolderClick: (FolderModel) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folderList[position]

        // Folder name set karna
        holder.binding.tvFolderName.text = folder.name

        // Folder ki total size calculate karke format karna
        val totalSizeBytes = folder.mediaList.sumOf { it.size }
        val formattedSize = formatFileSize(totalSizeBytes)

        // File count aur total size dikhana (e.g. "18 Files • 1.2 GB")
        holder.binding.tvItemCount.text = "${folder.mediaList.size} Files • $formattedSize"

        // Symbian 3D Icon set karna
        holder.binding.imgFolderIcon.setImageResource(R.drawable.bg_icon_3d)

        // Click listener
        holder.binding.root.setOnClickListener {
            onFolderClick(folder)
        }
    }

    override fun getItemCount(): Int = folderList.size

    // Helper Function: Bytes ko B, KB, MB, GB mein format karne ke liye
    private fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val size = sizeInBytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", size, units[digitGroups])
    }
}
