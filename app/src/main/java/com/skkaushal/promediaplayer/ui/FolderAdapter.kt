package com.skkaushal.promediaplayer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skkaushal.promediaplayer.R
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

        // Folder name set karna
        holder.binding.tvFolderName.text = folder.name

        // File count mapping (Symbian item_folder.xml ke ID ke hisab se)
        holder.binding.tvItemCount.text = "${folder.mediaList.size} Files"

        // Retro Symbian 3D Icon set karna
        holder.binding.imgFolderIcon.setImageResource(R.drawable.bg_icon_3d)

        // Item click listener
        holder.binding.root.setOnClickListener {
            onFolderClick(folder)
        }
    }

    override fun getItemCount(): Int = folderList.size
}
