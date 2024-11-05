package com.twnel.android_components.utils


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import java.io.File

@Composable
fun ImagePreview(
    onCancel: () -> Unit,
    onSend: () -> Unit = {},
    imagePath: String,
    sendText: String,
    cancelText: String,
    contentImagePreviewText: String,
    sendButton: Boolean = true
) {

    BackHandler {
        onCancel()
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(if (!sendButton) Color.Black.copy(alpha = 0.7f) else Color.Transparent)
            .padding(top = 8.dp),

        ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 50.dp, start = 6.dp, end = 6.dp)
                .fillMaxSize()

        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }

            Box(modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        val maxX = (size.width * (scale - 1)) / 2
                        val maxY = (size.height * (scale - 1)) / 2
                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                    }
                }) {
                val file = File(imagePath)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(file.absolutePath)
                        .size(Size(1024, 1024)).scale(Scale.FILL).crossfade(true).build(),
                    contentDescription = contentImagePreviewText,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .defaultMinSize(minWidth = 200.dp, minHeight = 200.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCancel, modifier = Modifier
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
                    onClick = onSend, modifier = Modifier
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
