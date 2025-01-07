package com.twnel.android_components.chat

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
