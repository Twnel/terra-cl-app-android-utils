package com.twnel.android_components.utils

import android.content.Context

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class VideoManager(private val context: Context) {
    val exoPlayer: ExoPlayer?
        get() = _exoPlayer

    private var _exoPlayer: ExoPlayer? = null
    var playbackSpeed: Float = 1.0f
        set(value) {
            field = value
            _exoPlayer?.setPlaybackSpeed(value)
        }

    fun playVideo(videoFile: String) {
        stopPlayback()

        _exoPlayer = ExoPlayer.Builder(context).build()
        _exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(videoFile))
            prepare()
            setPlaybackSpeed(playbackSpeed)
            play()
        }
    }

    fun pausePlayback() {
        _exoPlayer?.pause()
    }

    fun resumePlayback() {
        _exoPlayer?.play()
    }

    fun stopPlayback() {
        playbackSpeed = 1.0f
        _exoPlayer?.stop()
        _exoPlayer?.release()
        _exoPlayer = null
    }

    fun isPaused(messageId: String): Boolean {
        return /*currentlyPlayingVideoId == messageId && */_exoPlayer?.isPlaying == false
    }

    fun seekTo(position: Long) {
        _exoPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Long {
        return _exoPlayer?.currentPosition ?: 0
    }
}
