package com.skkaushal.promediaplayer.ui

import android.net.Uri

data class VideoModel(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val uri: Uri
)
