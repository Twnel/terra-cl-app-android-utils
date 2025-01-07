package com.twnel.android_components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.twnel.android_components.models.AbstractMessage
import java.io.File

@Composable
fun VideoContentMessage(
    message: AbstractMessage,
    setVideo: (AbstractMessage) -> Unit,
    onVideoClick: (String) -> Unit,
    messageThumbnailKey: String,
    messageThumbnailPath: String,
    stringPlayVideo: String,
) {
    if (message.pathFile.isNotEmpty() && messageThumbnailPath.isNotEmpty()) {
        val file = File(messageThumbnailPath)
        val (width, height) = setWidthAndHeightImage(message)

        Box(modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onVideoClick(message.pathFile) }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(file)
                    .size(width.value.toInt(), height.value.toInt()).scale(Scale.FILL)
                    .crossfade(true).build(),
                contentDescription = "",
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onVideoClick(message.pathFile) },
                contentScale = ContentScale.Crop
            )
            // Overlay the play button
            Icon(imageVector = Icons.Default.PlayArrow,
                contentDescription = stringPlayVideo,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f), shape = CircleShape
                    )
                    .padding(8.dp)
                    .clickable { onVideoClick(message.pathFile) })
        }
        Spacer(modifier = Modifier.size(8.dp))
    } else {
        setVideo(message)
        Box(modifier = Modifier
            .size(300.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
            .clickable { onVideoClick(message.media) }) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(messageThumbnailKey)
                .size(300).scale(Scale.FILL).crossfade(true).build(),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp)
                    )
                    .clickable { onVideoClick(message.media) })
            Icon(
                imageVector = Icons.Default.Downloading,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f), shape = CircleShape
                    )
                    .padding(8.dp)
            )
        }
    }
}
