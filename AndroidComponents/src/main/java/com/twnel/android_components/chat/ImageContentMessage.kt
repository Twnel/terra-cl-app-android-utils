package com.twnel.android_components.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.twnel.android_components.models.AbstractMessage
import java.io.File

@Composable
fun ImageContentMessage(
    message: AbstractMessage,
    setImage: (AbstractMessage) -> Unit,
    onImageClick: (String) -> Unit,
    stringImageLoading: String
) {
    if (message.pathFile.isNotEmpty()) {
        val file = File(message.pathFile)
        val (width, height) = setWidthAndHeightImage(message)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(file)
                .size(width.value.toInt(), height.value.toInt()).scale(Scale.FILL).crossfade(true)
                .build(),
            contentDescription = "",
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onImageClick(message.pathFile) },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.size(8.dp))
    } else {
        setImage(message)
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(message.media).size(300)
            .scale(Scale.FILL).crossfade(true).build(),
            contentDescription = stringImageLoading,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp)
                )
                .clickable { onImageClick(message.media) })
    }
}