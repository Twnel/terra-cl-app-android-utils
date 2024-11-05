package com.twnel.android_components.utils

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.floor
import kotlin.math.sqrt

class AudioManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var exoPlayer: ExoPlayer? = null
    private var currentlyPlayingAudioId: String? = null
    private var outputFile: File? = null
    var playbackSpeed: Float = 1.0f
        set(value) {
            field = value
            exoPlayer?.setPlaybackSpeed(value)
        }
    private val _currentlyPlayingMessageId = MutableStateFlow<String?>(null)
    val currentlyPlayingMessageId: StateFlow<String?> = _currentlyPlayingMessageId.asStateFlow()

    fun startRecording(toast: String): Boolean {
        stopRecording(toast)
        return try {
            outputFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000) // 128 kbps
                setAudioSamplingRate(44100) // 44.1 kHz
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            true
        } catch (e: IOException) {
            handleRecordingError(toast, e)
            false
        } catch (e: IllegalStateException) {
            handleRecordingError(toast, e)
            false
        } catch (e: Exception) {
            handleRecordingError(toast, e)
            false
        }
    }

    fun stopRecording(toast: String): Uri? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            outputFile?.let { Uri.fromFile(it) }
        } catch (e: RuntimeException) {
            handleRecordingError(toast, e)
            mediaRecorder?.release()
            mediaRecorder = null
            val delete = outputFile?.delete()
            Log.d("AudioManager", "File deleted: $delete")
            outputFile = null
            null
        }
    }

    fun cancelRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        val delete = outputFile?.delete()
        Log.d("AudioManager", "File deleted: $delete")
        outputFile = null
    }

    fun getMaxAmplitude(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    fun playAudio(audioFile: String, messageId: String, onCompletion: () -> Unit) {
        if (currentlyPlayingAudioId == messageId) {
            stopPlayback()
            return
        }

        stopPlayback()
        _currentlyPlayingMessageId.value = messageId

        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(audioFile))
            prepare()
            setPlaybackSpeed(playbackSpeed)
            play()
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        onCompletion()
                        currentlyPlayingAudioId = null
                    }
                }
            })
        }
        currentlyPlayingAudioId = messageId
    }

    fun pausePlayback() {
        exoPlayer?.pause()
    }

    fun resumePlayback() {
        exoPlayer?.play()
    }

    fun isPaused(messageId: String): Boolean {
        return currentlyPlayingAudioId == messageId && exoPlayer?.isPlaying == false
    }


    fun stopPlayback() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        currentlyPlayingAudioId = null
        _currentlyPlayingMessageId.value = null
    }

    fun isPlaying(messageId: String): Boolean {
        return currentlyPlayingAudioId == messageId && exoPlayer?.isPlaying == true
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    private fun handleRecordingError(toast: String, e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
        mediaRecorder?.release()
        mediaRecorder = null
        val delete = outputFile?.delete()
        Log.d("AudioManager", "File deleted: $delete")
        outputFile = null
    }

    fun extractAmplitudes(audioFilePath: String, windowSize: Int): List<Float> {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(audioFilePath)

            val bufferSize = 44100
            val buffer = ByteBuffer.allocate(bufferSize)

            extractor.selectTrack(0)

            val amplitudes = mutableListOf<Float>()
            var maxAmplitude = 0f
            while (true) {
                val bytesRead = extractor.readSampleData(buffer, 0)
                if (bytesRead < 0) break

                buffer.rewind()
                val windowStartIndex = amplitudes.size
                while (buffer.hasRemaining() && amplitudes.size - windowStartIndex < windowSize) {
                    val sample = buffer.short.toFloat().toInt()
                    val amplitude = calculateAmplitude(sample)
                    amplitudes.add(amplitude)
                    if (amplitude > maxAmplitude) maxAmplitude = amplitude
                }

                extractor.advance()
                buffer.clear()
            }
            normalizeAndDownSample(amplitudes, 50)

        } catch (e: Exception) {
            Log.e("WaveformGenerator", "Error generating waveform: ${e.message}")
            emptyList()
        } finally {
            extractor.release()
        }
    }

    private fun calculateAmplitude(sample: Int): Float {
        return sqrt((sample * sample).toFloat() / Short.MAX_VALUE.toFloat())
    }

    fun normalizeAndDownSample(amplitudes: List<Float>, sampleCount: Int): List<Float> {
        if (amplitudes.isEmpty()) return emptyList()

        val maxAmplitude = amplitudes.maxOrNull() ?: 1f
        val normalizedAmplitudes = amplitudes.map { it / maxAmplitude }

        val step = normalizedAmplitudes.size.toFloat() / sampleCount
        return List(sampleCount) { i ->
            val index = (i * step).toInt()
            val normalizedValue =
                normalizedAmplitudes[index.coerceIn(0, normalizedAmplitudes.lastIndex)]

            (floor(normalizedValue * 10_000) / 10_000)
        }
    }
}