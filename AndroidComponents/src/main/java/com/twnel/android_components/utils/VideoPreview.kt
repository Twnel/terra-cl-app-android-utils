package com.twnel.android_components.utils

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay


@OptIn(UnstableApi::class)
@Composable
fun VideoPreview(
    onCancel: () -> Unit,
    onSend: () -> Unit = {},
    videoPath: String,
    videoManager: VideoManager,
    sendText: String = "send",
    cancelText: String = "cancel",
    sendButton: Boolean = true
) {

    var isPlaying by remember { mutableStateOf(videoManager.exoPlayer?.isPlaying ?: true) }
    var playbackSpeed by remember { mutableFloatStateOf(videoManager.playbackSpeed) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var isFinished by remember { mutableStateOf(false) }

    BackHandler {
        videoManager.stopPlayback()
        onCancel()
    }

    LaunchedEffect(Unit) {
        while (true) {
            val player = videoManager.exoPlayer
            currentPosition = player?.currentPosition ?: 0L

            isFinished = player?.let {
                !it.isPlaying && currentPosition >= (it.duration - 100L)
            } ?: false

            delay(100)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (!sendButton) Color.Black.copy(alpha = 0.9f) else Color.Transparent)
            .padding(top = 50.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f))
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }

            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        val maxX = (size.width * (scale - 1)) / 2
                        val maxY = (size.height * (scale - 1)) / 2
                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                    }
                }) {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.Transparent
                ) {
                    AndroidView(
                        factory = { context ->
                            videoManager.playVideo(videoPath)
                            PlayerView(context).apply {
                                useController = false
                                player = videoManager.exoPlayer
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        }, modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }

                if (!isPlaying || isFinished) {
                    Icon(
                        imageVector = if (isFinished) Icons.Default.Replay else Icons.Default.PlayArrow,
                        contentDescription = "Replay/Play",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(8.dp)
                            .clickable {
                                isPlaying = true
                                if (isFinished) {
                                    isFinished = false
                                    videoManager.seekTo(0)
                                }
                                videoManager.resumePlayback()
                            },
                        tint = Color.White
                    )
                }

            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMilliseconds(currentPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formatMilliseconds(videoManager.exoPlayer?.duration ?: 0L),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { position ->
                    isFinished = false
                    videoManager.seekTo(position.toLong())
                },
                valueRange = 0f..(videoManager.exoPlayer?.duration?.toFloat() ?: 0f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            videoManager.resumePlayback()
                        } else {
                            videoManager.pausePlayback()
                        }
                    }, modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Button(
                    onClick = {
                        val newSpeed = when (playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            else -> 1.0f
                        }
                        videoManager.playbackSpeed = newSpeed
                        playbackSpeed = newSpeed
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .height(30.dp)
                        .width(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "${playbackSpeed}\u00D7",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    )
                }
            }
        }

        // Bottom Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    videoManager.stopPlayback()
                    onCancel()
                }, modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f), CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = cancelText,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (sendButton) {
                IconButton(
                    onClick = {
                        videoManager.stopPlayback()
                        onSend()
                    }, modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = sendText,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

private fun formatMilliseconds(milliseconds: Long): String {
    val minutes = (milliseconds / 1000) / 60
    val seconds = (milliseconds / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}

