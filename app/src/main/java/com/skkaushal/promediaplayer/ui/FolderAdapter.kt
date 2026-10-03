package com.skkaushal.promediaplayer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skkaushal.promediaplayer.databinding.ItemFolderBinding
import com.skkaushal.promediaplayer.model.FolderModel

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

        holder.binding.tvFolderName.text = folder.name
        holder.binding.tvFileCount.text = "${folder.mediaList.size} Files"

        // Full Card Root Click Listener
        holder.binding.root.setOnClickListener {
            onFolderClick(folder)
        }
    }

    override fun getItemCount(): Int = folderList.size
}
