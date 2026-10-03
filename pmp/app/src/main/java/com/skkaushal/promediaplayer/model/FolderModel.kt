package com.skkaushal.promediaplayer.model

data class FolderModel(
    val id: String,
    val name: String,
    val mediaList: List<MediaItemModel>
)
