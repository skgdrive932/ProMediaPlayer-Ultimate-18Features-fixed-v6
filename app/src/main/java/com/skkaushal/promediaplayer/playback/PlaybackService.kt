package com.skkaushal.promediaplayer.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService: MediaSessionService() {
    private var player: ExoPlayer?=null
    private var session: MediaSession?=null
    override fun onCreate() {
        super.onCreate()
        player=ExoPlayer.Builder(this).setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),true
        ).build()
        session=MediaSession.Builder(this,player!!).setSessionActivity(
            PendingIntent.getActivity(this,0,Intent(this,Class.forName("com.skkaushal.promediaplayer.ui.MainActivity")),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        ).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo)=session
    override fun onDestroy(){session?.release(); player?.release(); super.onDestroy()}
}
