package com.twnel.android_components.chat

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.twnel.android_components.UIConstant
import com.twnel.android_components.models.AbstractMessage
import com.twnel.android_components.utils.ImageTypeSource
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ResendButton(
    onResendClick: () -> Unit,
    stringRetrying: String,
    stringRetrySending: String,
    modifier: Modifier
) {
    var isLoading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isLoading) {
                isLoading = true
                onResendClick()
            }
        },
        modifier = modifier,
        contentPadding = PaddingValues(3.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(15.dp),
                color = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            if (isLoading) "$stringRetrying..." else stringRetrySending,
            style = MaterialTheme.typography.labelMedium
        )
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000)
            isLoading = false
        }
    }
}


@Composable
private fun ImageInfo(
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

@Composable
private fun ImageContentMessage(
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

private fun setWidthAndHeightImage(message: AbstractMessage): Pair<Dp, Dp> {
    var width = 300.dp
    var height = 300.dp
    if (message.aspectRatio > 1) {
        height /= message.aspectRatio
    } else if (message.aspectRatio < 1) {
        width *= message.aspectRatio
    }
    return Pair(width, height)
}

@Composable
fun PulsingRecordingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = modifier
            .background(Color.Red.copy(alpha = alpha), CircleShape)
            .size(10.dp)
    )
}

fun formatRecordingDuration(durationInSeconds: Int): String {
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun OptionButton(
    iconResource: ImageVector, description: String, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconResource,
                contentDescription = description,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    closeCamera: () -> Unit = {},
    onTakePhoto: (Bitmap) -> Unit,
    stringCloseCamera: String,
    stringSwitchCamera: String,
    stringTakePhoto: String
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE,
            )
        }
    }
    val lifecycle = LocalLifecycleOwner.current

    BackHandler {
        closeCamera()
    }

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    if (permissionState.status.isGranted) {
        Box {
            CamaraComposable(cameraController, lifecycle)
            IconButton(onClick = { closeCamera() }) {
                Icon(
                    imageVector = Icons.Default.Cancel, contentDescription = stringCloseCamera
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = {
                    cameraController.cameraSelector =
                        if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA
                }) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = stringSwitchCamera
                    )
                }
                IconButton(onClick = {
                    takePhoto(cameraController, onTakePhoto, context)
                }) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = stringTakePhoto
                    )
                }
            }
        }

    }
}

@Composable
fun CamaraComposable(
    cameraController: LifecycleCameraController,
    lifecycle: LifecycleOwner,
    modifier: Modifier = Modifier,
) {
    cameraController.bindToLifecycle(lifecycle)
    AndroidView(modifier = modifier, factory = { context ->
        val previewView = PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        previewView.controller = cameraController

        previewView
    })
}

@Composable
fun PickImageFromGallery(stringPickImage: String) {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        imageUri?.let {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap.value = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                bitmap.value = ImageDecoder.decodeBitmap(source)
            }

            bitmap.value?.let { btm ->
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(400.dp)
                        .padding(20.dp)
                )
            }
        }

        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = stringPickImage)
        }
    }

}

private fun takePhoto(
    controller: LifecycleCameraController, onPhotoTaken: (Bitmap) -> Unit, context: Context
) {
    controller.takePicture(ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(), 0, 0, image.width, image.height, matrix, true
                )

                onPhotoTaken(rotatedBitmap)
            }
        })
}




