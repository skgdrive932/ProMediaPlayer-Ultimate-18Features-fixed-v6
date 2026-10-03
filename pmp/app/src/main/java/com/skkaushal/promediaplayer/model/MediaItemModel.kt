package com.skkaushal.promediaplayer.model

data class MediaItemModel(
    val id: Long,
    val title: String,
    val uri: String,
    val duration: Long,
    val isVideo: Boolean,
    val mime: String = "",
    val size: Long = 0L,
    val width: Int = 0,
    val height: Int = 0
)
