package com.twnel.android_components.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage


@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    imageUrl: String = "",
    profileContentDescription: String,
    editContentDescription: String,
//    onRemoveImage: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = profileContentDescription,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { showDialog = true },
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                imageVector = Icons.Default.SentimentSatisfiedAlt,
                contentDescription = profileContentDescription,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { showDialog = true },
                contentScale = ContentScale.Crop
            )
        }
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 15.dp, y = (8).dp)
            .padding(8.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .clickable { onEditClick() }
            .wrapContentSize(Alignment.Center)) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = editContentDescription,
                modifier = Modifier.size(25.dp)
            )
        }
    }
    EnlargeImageDialog(
        showDialog = showDialog,
        onDismissRequest = { showDialog = false },
        imageUrl = imageUrl,
        profileContentDescription = profileContentDescription
//        onRemoveImage
    )
}

@Composable
fun EnlargeImageDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    imageUrl: String,
    profileContentDescription: String,
//    onRemoveImage: () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismissRequest
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = profileContentDescription,
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = profileContentDescription,
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                }
//                Button(
//                    onClick = onRemoveImage,
//                    modifier = Modifier
//                        .padding(top = 16.dp)
//                ) {
//                    Text("Remove Image")
//                }
            }
        }
    }
}
