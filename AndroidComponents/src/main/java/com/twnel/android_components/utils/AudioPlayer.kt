package com.twnel.android_components.utils


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun AudioPlayer(
    audioPath: String,
    messageId: String,
    audioManager: AudioManager,
    waveformString: String,
    audioDuration: Int,
    transcript: String,
    scroll: () -> Unit,
    lastItem: Boolean = false,
    owner: String,
    playText: String,
    pauseText: String,
    transcriptTitle: String = "",
    author: String = ""
) {

    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var playbackSpeed by remember { mutableFloatStateOf(audioManager.playbackSpeed) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showTranscript by remember { mutableStateOf(false) }
    val error by remember { mutableStateOf<String?>(null) }
    val waveform = remember(waveformString) {
        if (waveformString.isBlank()) {
            emptyList()
        } else {
            waveformString.removeSurrounding("[", "]").split(",").map { it.toFloatOrNull() ?: 0f }
        }
    }

    val currentlyPlayingMessageId by audioManager.currentlyPlayingMessageId.collectAsState()

    DisposableEffect(messageId) {
        onDispose {
            if (audioManager.isPlaying(messageId) || audioManager.isPaused(messageId)) {
                audioManager.stopPlayback()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            isPlaying = audioManager.isPlaying(messageId)
            isPaused = audioManager.isPaused(messageId)
            if (isPlaying || isPaused) {
                duration = audioManager.getDuration()
                currentTime = audioManager.getCurrentPosition()
                progress = audioManager.getCurrentPosition().toFloat() / duration
            }
            delay(100)
        }
    }


    LaunchedEffect(currentlyPlayingMessageId) {
        if (currentlyPlayingMessageId != messageId) {
            progress = 0f
            isPlaying = false
            isPaused = false
        }
    }

    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val backgroundColor = MaterialTheme.colorScheme.background
    val blendedColor = blendColors(backgroundColor, secondaryContainerColor, 0.5f)

    Row(
        Modifier
            .fillMaxSize()
            .heightIn(min = 70.dp)
            .padding(top = 10.dp),

        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .border(
                    0.8.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(start = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioIcon(
                    isPlaying = isPlaying,
                    isPaused = isPaused,
                    audioManager = audioManager,
                    audioPath = audioPath,
                    messageId = messageId,
                    setProgress = { newProgress -> progress = newProgress },
                    error = error,
                    playText = playText,
                    pauseText = pauseText,
                    blendedColor = blendedColor
                )
                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    WaveformVisualizer(
                        waveformData = waveform,
                        progress = progress,
                        onProgressChange = { newProgress ->
                            progress = newProgress
                            if (isPlaying || isPaused) {
                                audioManager.seekTo((newProgress * duration).toLong())
                            } else {
                                audioManager.playAudio(audioPath, messageId) {
                                    progress = 0f
                                }
                                audioManager.seekTo((newProgress * duration).toLong())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (error != null) "Error" else if (isPlaying || isPaused) {
                        formatRecordingDuration(currentTime.toInt() / 1000)
                    } else {
                        formatRecordingDuration(audioDuration)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                AudioVelocity(
                    audioManager = audioManager,
                    isPlaying = isPlaying,
                    isPaused = isPaused,
                    blendedColor = blendedColor,
                    playbackSpeed = playbackSpeed,
                    setPlaybackSpeed = { newSpeed -> playbackSpeed = newSpeed },
                    showTranscript = showTranscript,
                    setShowTranscript = { showTranscript = it },
                    author = author,
                    owner = owner
                )

            }
            AnimatedVisibility(
                visible = showTranscript,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top,
                    initialHeight = { 0 }),
                exit = fadeOut() + shrinkVertically()

            ) {
                Box(
                    modifier = Modifier.padding(
                        top = 2.dp, start = 10.dp, end = 10.dp, bottom = 10.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.2f
                                )
                            )
                    ) {
                        Transcript(transcript = transcript, transcriptTitle = transcriptTitle)
                    }
                }
                if (lastItem) scroll()
            }
        }
        error?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AudioIcon(
    isPlaying: Boolean,
    isPaused: Boolean,
    audioManager: AudioManager,
    audioPath: String,
    messageId: String,
    setProgress: (Float) -> Unit,
    error: String?,
    playText: String,
    pauseText: String,
    blendedColor: Color
) {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.size(35.dp)
    ) {
        IconButton(
            onClick = {
                when {
                    isPlaying -> audioManager.pausePlayback()
                    isPaused -> audioManager.resumePlayback()
                    else -> audioManager.playAudio(audioPath, messageId) {
                        setProgress(0f)
                    }
                }
            },
            enabled = error == null,
            modifier = Modifier.fillMaxSize(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary, contentColor = blendedColor
            )
        ) {
            Icon(
                imageVector = when {
                    isPlaying -> Icons.Rounded.Pause
                    isPaused -> Icons.Rounded.PlayArrow
                    else -> Icons.Rounded.PlayArrow
                },
                contentDescription = if (isPlaying) pauseText else playText,
                modifier = Modifier.size(28.dp),
                tint = blendedColor
            )
        }
    }
}

@Composable
fun AudioVelocity(
    audioManager: AudioManager,
    isPlaying: Boolean,
    isPaused: Boolean,
    author: String,
    owner: String,
    blendedColor: Color,
    playbackSpeed: Float,
    setPlaybackSpeed: (Float) -> Unit,
    transcriptTitle: String = "",
    showTranscript: Boolean = false,  // Default value
    setShowTranscript: ((Boolean) -> Unit)? = null  // Nullable lambda
) {
    Box(
        modifier = Modifier.size(45.dp)
    ) {
        if (isPlaying || isPaused || author == owner) {
            Button(
                onClick = {
                    val newSpeed = when (playbackSpeed) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                    audioManager.playbackSpeed = newSpeed
                    setPlaybackSpeed(newSpeed)
                },
                modifier = Modifier
                    .height(20.dp)
                    .width(35.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = blendedColor
                )
            ) {
                Text(
                    text = "${playbackSpeed}\u00D7",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                )
            }
        } else if (setShowTranscript != null) {
            Icon(imageVector = if (showTranscript) Icons.Default.Description else Icons.Outlined.Description,
                contentDescription = transcriptTitle,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .clickable {
                        setShowTranscript(!showTranscript)
                    })
        }
    }
}


@Composable
fun Transcript(
    transcript: String, transcriptTitle: String
) {
    Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(
            text = transcriptTitle, style = MaterialTheme.typography.titleSmall
        )
        if (transcript.isNotEmpty()) {
            Text(
                text = transcript.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 144.dp)
                    .verticalScroll(rememberScrollState())
            )
        } else {
            LoadingAnimation(
                circleSize = 12.dp,
                circleColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Composable
fun blendColors(background: Color, overlay: Color, overlayAlpha: Float): Color {
    val r = (overlay.red * overlayAlpha + background.red * (1 - overlayAlpha))
    val g = (overlay.green * overlayAlpha + background.green * (1 - overlayAlpha))
    val b = (overlay.blue * overlayAlpha + background.blue * (1 - overlayAlpha))
    return Color(r, g, b, 1f)
}

@Composable
fun WaveformVisualizer(
    waveformData: List<Float>,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primColor = MaterialTheme.colorScheme.secondary
    val surfColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    val waveform = if (waveformData.all { it.isNaN() }) emptyList()
    else normalizeAndClampWaveform(waveformData)



    Canvas(modifier = modifier
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                onProgressChange(newProgress)
            }
        }
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                onProgressChange(newProgress)
            }
        }) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / waveform.size
        val progressWidth = canvasWidth * progress

        if (waveform.isEmpty() || waveform.size < 10) {
            drawLine(
                color = surfColor,
                start = Offset(0f, canvasHeight / 2),
                end = Offset(canvasWidth, canvasHeight / 2),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = primColor,
                start = Offset(0f, canvasHeight / 2),
                end = Offset(progressWidth, canvasHeight / 2),
                strokeWidth = 2.dp.toPx()
            )
        } else {
            waveform.forEachIndexed { index, amplitude ->
                val barHeight = amplitude * canvasHeight * 0.6f
                val startX = index * barWidth
                val fillRatio = ((progressWidth - startX) / barWidth).coerceIn(0f, 1f)
                val barColor = Color(
                    red = lerp(surfColor.red, primColor.red, fillRatio),
                    green = lerp(surfColor.green, primColor.green, fillRatio),
                    blue = lerp(surfColor.blue, primColor.blue, fillRatio),
                    alpha = lerp(surfColor.alpha, primColor.alpha, fillRatio)
                )

                drawLine(
                    color = barColor,
                    start = Offset(startX, canvasHeight / 2 - barHeight / 2),
                    end = Offset(startX, canvasHeight / 2 + barHeight / 2),
                    strokeWidth = barWidth * 0.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        drawCircle(
            color = Color.Transparent,
            radius = 5.dp.toPx(),
            center = Offset(progressWidth, canvasHeight / 2)
        )
    }
}

fun normalizeAndClampWaveform(waveform: List<Float>): List<Float> {
    val maxValue = waveform.maxOrNull() ?: return emptyList()
    val minValue = waveform.minOrNull() ?: return emptyList()

    val range = maxValue - minValue
    if (range == 0f) return waveform.map { 0f }

    return waveform.map { value ->
        ((value - minValue) / range).coerceIn(0f, 1f)
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return (start + fraction * (end - start)).coerceIn(0f, 1f)
}


fun formatRecordingDuration(durationInSeconds: Int): String {
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

