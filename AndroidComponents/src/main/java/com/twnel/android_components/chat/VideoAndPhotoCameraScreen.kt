package com.twnel.android_components.chat

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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

    val isPressingAction = { cameraController: LifecycleCameraController ->
        if (isPressing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isPhotoMode = false
            isRecording = true
            activeRecording = startVideoRecording(
                cameraController, onVideoRecorded, context
            ) {}
        }
    }

    val selectCamera = { cameraController: LifecycleCameraController ->
        cameraController.cameraSelector =
            if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
    }

    val onTakePhotoOrVideo = { cameraController: LifecycleCameraController ->
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

            CloseIcon(isRecording, closeCamera, stringCloseCamera)
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
                                        isPressingAction(cameraController)
                                    }

                                    try {
                                        awaitRelease()
                                    } finally {
                                        isPressing = false
                                        pressJob.cancel()
                                        onTakePhotoOrVideo(cameraController)
                                    }
                                })
                            })
                    }
                    val modifierIcon = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                    SwitchCameraIcon(
                        isRecording, cameraController, selectCamera, stringSwitchCamera, modifierIcon
                    )
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
fun SwitchCameraIcon(
    isRecording: Boolean,
    cameraController: LifecycleCameraController,
    selectCamera: (LifecycleCameraController) -> Unit,
    stringSwitchCamera: String,
    modifier: Modifier = Modifier
){
    if (!isRecording) {
        IconButton(
            onClick = {
                selectCamera(cameraController)
            }, modifier = modifier
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

@Composable
fun CloseIcon(isRecording: Boolean, closeCamera: () -> Unit, stringCloseCamera: String) {
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
