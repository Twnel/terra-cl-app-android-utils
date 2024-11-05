package com.twnel.android_components.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.twnel.android_components.UIConstant


@Composable
fun ImageTypeSource(
    imageType: String,
    image: Any,
    description: String,
    size: Dp = 60.dp,
    iconSize: Dp? = null,
    roundCorner: Dp = 16.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary
) {
    when (imageType) {
        UIConstant.EXTERNAL_IMAGE -> {
            AsyncImage(
                model = image as String,
                contentDescription = description,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(roundCorner))
            )
        }

        UIConstant.ICON_IMAGE -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(roundCorner))
                    .background(backgroundColor)
                    .size(size)
            ) {
                Icon(
                    imageVector = image as ImageVector,
                    contentDescription = description,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(iconSize ?: size),
                    tint = MaterialTheme.typography.titleMedium.color
                )
            }
        }


        UIConstant.LOCAL_IMAGE -> {
            Image(
                painter = image as Painter,
                contentDescription = description,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(roundCorner)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
