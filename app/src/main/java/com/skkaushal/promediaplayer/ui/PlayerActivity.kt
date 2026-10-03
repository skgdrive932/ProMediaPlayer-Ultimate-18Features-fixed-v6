package com.skkaushal.promediaplayer.ui

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.skkaushal.promediaplayer.databinding.ActivityPlayerBinding
import kotlin.math.abs

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var controlsVisible = true
    private var locked = false
    private var maxVolume = 0
    private var currentBrightness = 0.5f

    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        currentBrightness = window.attributes.screenBrightness.takeIf { it > 0f } ?: 0.5f

        val mediaUri = resolveVideoUri(intent)
        val videoName = getDisplayNameFromUri(mediaUri)
        binding.tvVideoName.text = videoName ?: "Video"

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRotate.setOnClickListener { toggleScreenRotation() }
        binding.btnPlayPause.setOnClickListener { togglePlayPause() }
        binding.btnRewind.setOnClickListener { seekBy(-10_000L) }
        binding.btnForward.setOnClickListener { seekBy(10_000L) }
        binding.btnLock.setOnClickListener { toggleLock() }
        binding.btnMore.setOnClickListener {
            Toast.makeText(this, "More playback options coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = player?.duration ?: C.TIME_UNSET
                    if (duration != C.TIME_UNSET && duration > 0) {
                        player?.seekTo((duration * progress / 1000L).coerceIn(0L, duration))
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        setupGestures()
        initPlayer(mediaUri)
        handler.post(progressRunnable)
    }

    /**
     * Resolve the video URI from every supported launch path.
     * Primary path: Intent.data. Fallback: explicit video_uri extra.
     * Also accepts ClipData for Android document/media pickers.
     */
    private fun resolveVideoUri(intent: android.content.Intent): Uri? {
        intent.data?.let { return it }
        intent.getStringExtra("video_uri")?.let { raw ->
            runCatching { Uri.parse(raw) }.getOrNull()?.let { return it }
        }
        intent.clipData?.let { clip ->
            if (clip.itemCount > 0) clip.getItemAt(0).uri?.let { return it }
        }
        return null
    }

    private fun initPlayer(mediaUri: Uri?) {
        if (mediaUri == null) {
            showPlayerMessage("Video URI not found")
            return
        }

        try {
            player = ExoPlayer.Builder(this).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    true
                )
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            binding.tvPlayerMessage.visibility = View.GONE
                            updatePlayPauseIcon()
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            updatePlayPauseIcon()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlayPauseIcon()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        val message = "Playback failed: ${error.errorCodeName}"
                        showPlayerMessage(message)
                        Toast.makeText(this@PlayerActivity, message, Toast.LENGTH_LONG).show()
                    }
                })

                // Let Media3 choose the correct extractor/decoder for the local MediaStore URI.
                setMediaItem(MediaItem.fromUri(mediaUri))
                prepare()
                playWhenReady = true
            }
            binding.playerView.player = player
        } catch (e: Exception) {
            showPlayerMessage("Unable to open video: ${e.message ?: "unknown error"}")
        }
    }

    private fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) it.pause() else it.play()
            updatePlayPauseIcon()
        }
    }

    private fun updatePlayPauseIcon() {
        binding.btnPlayPause.setImageResource(
            if (player?.isPlaying == true) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun seekBy(delta: Long) {
        player?.let {
            val duration = it.duration
            val position = (it.currentPosition + delta).coerceIn(0L, if (duration > 0) duration else Long.MAX_VALUE)
            it.seekTo(position)
            showStatus(formatTime(position))
        }
    }

    private fun updateProgress() {
        val p = player ?: return
        val duration = p.duration
        val position = p.currentPosition
        binding.tvCurrentTime.text = formatTime(position)
        binding.tvTotalTime.text = if (duration > 0) formatTime(duration) else "00:00"
        if (duration > 0 && !binding.seekBar.isPressed) {
            binding.seekBar.progress = (position * 1000L / duration).toInt().coerceIn(0, 1000)
        }
        updatePlayPauseIcon()
    }

    private fun toggleLock() {
        locked = !locked
        binding.topBar.visibility = if (locked) View.GONE else View.VISIBLE
        binding.centerControls.visibility = if (locked) View.GONE else View.VISIBLE
        binding.bottomBar.visibility = if (locked) View.GONE else View.VISIBLE
        binding.btnLock.visibility = View.VISIBLE
        showStatus(if (locked) "Controls locked" else "Controls unlocked")
    }

    private fun toggleScreenRotation() {
        val currentOrientation = resources.configuration.orientation
        requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    private fun setupGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (locked) {
                    locked = false
                    binding.topBar.visibility = View.VISIBLE
                    binding.centerControls.visibility = View.VISIBLE
                    binding.bottomBar.visibility = View.VISIBLE
                } else {
                    setControlsVisible(!controlsVisible)
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (!locked) {
                    if (e.x < binding.playerView.width / 2f) seekBy(-10_000L) else seekBy(10_000L)
                }
                return true
            }

            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if (locked || e1 == null || player == null) return false
                val width = binding.playerView.width.coerceAtLeast(1)
                val height = binding.playerView.height.coerceAtLeast(1)
                if (abs(distanceX) > abs(distanceY)) {
                    val delta = (-distanceX / width * 90_000L).toLong()
                    player?.let { p ->
                        val duration = p.duration
                        if (duration > 0) p.seekTo((p.currentPosition + delta).coerceIn(0L, duration))
                    }
                    showStatus("${if (distanceX < 0) ">>" else "<<"} ${formatTime(player?.currentPosition ?: 0L)}")
                } else {
                    val delta = (e1.y - e2.y) / height
                    if (e1.x < width / 2f) {
                        currentBrightness = (currentBrightness + delta).coerceIn(0.05f, 1f)
                        val lp = window.attributes
                        lp.screenBrightness = currentBrightness
                        window.attributes = lp
                        showStatus("Brightness ${(currentBrightness * 100).toInt()}%")
                    } else {
                        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val next = (current + (delta * maxVolume).toInt()).coerceIn(0, maxVolume)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                        showStatus("Volume ${if (maxVolume == 0) 0 else next * 100 / maxVolume}%")
                    }
                }
                return true
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                handler.postDelayed({ if (!locked) binding.tvGestureStatus.visibility = View.GONE }, 500)
            }
            true
        }
    }

    private fun setControlsVisible(visible: Boolean) {
        controlsVisible = visible
        val v = if (visible) View.VISIBLE else View.GONE
        binding.topBar.visibility = v
        binding.centerControls.visibility = v
        binding.bottomBar.visibility = v
    }

    private fun showStatus(text: String) {
        binding.tvGestureStatus.text = text
        binding.tvGestureStatus.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages(binding.tvGestureStatus)
        handler.postDelayed({ binding.tvGestureStatus.visibility = View.GONE }, 900)
    }

    private fun showPlayerMessage(text: String) {
        binding.tvPlayerMessage.text = text
        binding.tvPlayerMessage.visibility = View.VISIBLE
    }

    private fun getDisplayNameFromUri(uri: Uri?): String? {
        if (uri == null) return null
        var name: String? = null
        contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) name = c.getString(0)
        }
        return name ?: uri.lastPathSegment
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        handler.removeCallbacks(progressRunnable)
        binding.playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }
}
