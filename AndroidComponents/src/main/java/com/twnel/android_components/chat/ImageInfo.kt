package com.twnel.android_components.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.twnel.android_components.UIConstant
import com.twnel.android_components.utils.ImageTypeSource

@Composable
fun ImageInfo(
    showInfo: Boolean = true, image: Any, color: Color, imageType: String, stringCompanyLogo: String
) {
    if (showInfo) {
        val imgSrc: Any = if (image != "") image else Icons.Default.SentimentSatisfiedAlt
        val roundCorner = if (image != "") 16.dp else 8.dp
        if (image != "" && imageType != UIConstant.LOCAL_IMAGE) {
            SubcomposeAsyncImage(model = imgSrc, contentDescription = "Contact") {
                val state = painter.state
                if (state is AsyncImagePainter.State.Success) {
                    ImageTypeSource(
                        imageType = imageType,
                        image = imgSrc,
                        description = "Contact",
                        size = 50.dp,
                        roundCorner = roundCorner,
                        backgroundColor = color
                    )
                } else {
                    ImageTypeSource(
                        imageType = UIConstant.ICON_IMAGE,
                        image = Icons.Default.SentimentSatisfiedAlt,
                        description = "Contact",
                        size = 50.dp,
                        roundCorner = 8.dp,
                        backgroundColor = color
                    )
                }
            }
        } else {
            ImageTypeSource(
                imageType = imageType,
                image = imgSrc,
                description = stringCompanyLogo,
                size = 50.dp,
                roundCorner = roundCorner,
                backgroundColor = color
            )
        }
    }
}
