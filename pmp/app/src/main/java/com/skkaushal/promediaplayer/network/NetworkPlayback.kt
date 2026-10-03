package com.skkaushal.promediaplayer.network

import android.net.Uri
import androidx.media3.common.MediaItem

object NetworkPlayback {
    fun mediaItem(url:String): MediaItem = MediaItem.fromUri(Uri.parse(url))
    fun isSupportedUrl(url:String):Boolean =
        url.startsWith("http://",true) || url.startsWith("https://",true)
}
