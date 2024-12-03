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
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.twnel.android_components.UIConstant
import com.twnel.android_components.models.AbstractMessage
import com.twnel.android_components.utils.ImageTypeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoCameraScreen(
    closeCamera: () -> Unit = {},
    onTakePhoto: (Bitmap) -> Unit,
    captureInstructionString: String,
    stringCloseCamera: String,
    stringSwitchCamera: String
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var isTapped by remember { mutableStateOf(false) }


    val recordButtonColor by animateColorAsState(
        targetValue = if (isTapped) Color.White.copy(alpha = 0.8f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = ""
    )

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    val allPermissionsGranted = permissionsState.allPermissionsGranted

    LaunchedEffect(Unit) {
        if (!allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (allPermissionsGranted) {
        val cameraController = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            }
        }

        BackHandler {
            closeCamera()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CamaraComposable(cameraController, lifecycle)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = closeCamera, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringCloseCamera,
                        tint = Color.White
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(70.dp)
                            .border(
                                width = 4.dp, color = Color.White, shape = CircleShape
                            )
                            .padding(6.dp)
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = recordButtonColor, shape = CircleShape
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(onPress = {
                                    isTapped = true
                                    coroutineScope.launch {
                                        delay(150)
                                        isTapped = false
                                    }

                                    try {
                                        awaitRelease()
                                    } finally {
                                        takePhoto(cameraController, onTakePhoto, context)
                                    }
                                })
                            })
                    }
                    IconButton(
                        onClick = {
                            cameraController.cameraSelector =
                                if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                                else CameraSelector.DEFAULT_BACK_CAMERA
                        }, modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = stringSwitchCamera,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = captureInstructionString,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
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
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoAndPhotoCameraScreen(
    closeCamera: () -> Unit = {},
    onTakePhoto: (Bitmap) -> Unit,
    onVideoRecorded: (Uri) -> Unit,
    captureInstructionString: String,
    stringCloseCamera: String,
    stringSwitchCamera: String
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    var isRecording by remember { mutableStateOf(false) }
    var activeRecording: Recording? by remember { mutableStateOf(null) }
    var isPhotoMode by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()
    var isPressing by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val recordButtonSize by animateDpAsState(
        targetValue = if (isRecording) 75.dp else 70.dp,
        animationSpec = spring(dampingRatio = 0.7f),
        label = ""
    )
    val recordButtonColor by animateColorAsState(
        targetValue = when {
            isRecording -> Color.Red.copy(alpha = 0.8f)
            isTapped -> Color.White.copy(alpha = 0.8f)
            else -> Color.Transparent
        }, animationSpec = tween(durationMillis = 300), label = ""
    )
    val dotOpacity by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val permissions = remember {
        mutableListOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    val allPermissionsGranted = permissionsState.allPermissionsGranted

    LaunchedEffect(Unit) {
        if (!allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (isRecording) {
        LaunchedEffect(Unit) {
            val startTime = System.currentTimeMillis()
            while (isRecording) {
                elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    } else {
        elapsedTime = 0L
    }

    if (allPermissionsGranted) {
        val cameraController = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                )
            }
        }

        BackHandler {
            closeCamera()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CamaraComposable(cameraController, lifecycle)

            if (isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .align(Alignment.TopCenter), contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = dotOpacity))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(
                                "%02d:%02d", elapsedTime / 60, elapsedTime % 60
                            ),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isRecording) {
                    IconButton(onClick = closeCamera, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringCloseCamera,
                            tint = Color.White
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(recordButtonSize)
                            .border(
                                width = 4.dp, color = Color.White, shape = CircleShape
                            )
                            .padding(6.dp)
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = recordButtonColor, shape = CircleShape
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(onPress = {
                                    isTapped = true
                                    coroutineScope.launch {
                                        delay(150)
                                        isTapped = false
                                    }
                                    isPressing = true

                                    val pressJob = coroutineScope.launch {
                                        delay(300)
                                        if (isPressing) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isPhotoMode = false
                                            isRecording = true
                                            activeRecording = startVideoRecording(
                                                cameraController, onVideoRecorded, context
                                            ) {}
                                        }
                                    }

                                    try {
                                        awaitRelease()
                                    } finally {
                                        isPressing = false
                                        pressJob.cancel()
                                        if (isRecording) {
                                            activeRecording?.let { it1 ->
                                                stopVideoRecording(
                                                    it1
                                                )
                                            }
                                            isRecording = false
                                            isPhotoMode = true
                                        } else if (isPhotoMode) {
                                            takePhoto(
                                                cameraController, onTakePhoto, context
                                            )
                                        }
                                    }
                                })
                            })
                    }
                    if (!isRecording) {
                        IconButton(
                            onClick = {
                                cameraController.cameraSelector =
                                    if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                                    else CameraSelector.DEFAULT_BACK_CAMERA
                            }, modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = stringSwitchCamera,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = captureInstructionString,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}


private fun startVideoRecording(
    controller: LifecycleCameraController,
    onVideoRecorded: (Uri) -> Unit,
    context: Context,
    onRecordingStarted: () -> Unit
): Recording {
    val videoFile =
        File(context.getExternalFilesDir(null), "video_${System.currentTimeMillis()}.mp4")
    val outputOptions = FileOutputOptions.Builder(videoFile).build()

    val audioConfig = AudioConfig.create(true) // Enables audio recording

    val recording = controller.startRecording(
        outputOptions, audioConfig, ContextCompat.getMainExecutor(context)
    ) { event ->
        when (event) {
            is VideoRecordEvent.Start -> {
                onRecordingStarted()
            }

            is VideoRecordEvent.Finalize -> {
                if (event.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                    onVideoRecorded(event.outputResults.outputUri)
                } else {
                    Toast.makeText(
                        context, "Video recording failed: ${event.error}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    return recording
}

private fun stopVideoRecording(recording: Recording) {
    recording.stop()
}
