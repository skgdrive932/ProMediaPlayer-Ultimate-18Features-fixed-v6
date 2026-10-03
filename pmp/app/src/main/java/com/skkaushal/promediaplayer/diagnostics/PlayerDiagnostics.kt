package com.skkaushal.promediaplayer.diagnostics

import androidx.media3.common.Format
import androidx.media3.exoplayer.ExoPlayer

data class Diagnostics(
    val videoSize:String, val durationMs:Long, val positionMs:Long,
    val bufferedMs:Long, val videoFormat:String?, val audioFormat:String?
)
fun ExoPlayer.diagnostics(): Diagnostics = Diagnostics(
    "${videoSize.width}x${videoSize.height} @ ${videoSize.pixelWidthHeightRatio}",
    duration, currentPosition, bufferedPosition-currentPosition,
    videoFormat?.let { "${it.sampleMimeType} ${it.bitrate}bps" },
    audioFormat?.let { "${it.sampleMimeType} ${it.channelCount}ch ${it.sampleRate}Hz" }
)
