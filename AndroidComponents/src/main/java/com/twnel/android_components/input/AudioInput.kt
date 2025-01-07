package com.twnel.android_components.input

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.twnel.android_components.utils.PermissionContent
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordButton(
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    closeText: String,
    cancelText: String,
    contentRecordText: String,
    allowAudioText: String,
    enableAudioText: String,
    onDragOffsetChange: (Float) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val cancelThreshold = -80f
    val permission = android.Manifest.permission.RECORD_AUDIO
    var permissionNeeded by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(permission = permission)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                permissionNeeded = true
            }
        }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f, animationSpec = infiniteRepeatable(
            animation = tween(500), repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val buttonSize by animateDpAsState(
        targetValue = if (isRecording) 60.dp else 54.dp, label = ""
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            dragOffset < cancelThreshold -> MaterialTheme.colorScheme.onTertiary
            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)//Color.Red
        }, label = ""
    )

    val scaleContent = if (isRecording) scale else 1f

    val onDragEndAction = {
        isRecording = false
        if (dragOffset < cancelThreshold) {
            onCancelRecording()
        } else {
            onStopRecording()
        }
        dragOffset = 0f
        onDragOffsetChange(0f)
    }

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(buttonSize)
            .offset { IntOffset(dragOffset.roundToInt(), 0) }
            .graphicsLayer {
                scaleX = scaleContent
                scaleY = scaleContent
            }
            .clip(CircleShape)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    when {
                        permissionState.status.isGranted -> {
                            isRecording = true
                            onStartRecording()
                            awaitRelease()
                            isRecording = false
                            onStopRecording()
                        }

                        permissionState.status.shouldShowRationale -> {
                            permissionNeeded = true
                        }

                        else -> {
                            launcher.launch(permission)
                        }
                    }
                })
            }
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = {},
                    onDragEnd = { onDragEndAction() },
                    onDragCancel = {
                        isRecording = false
                        onCancelRecording()
                        dragOffset = 0f
                        onDragOffsetChange(0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset =
                            (dragOffset + dragAmount.x).coerceIn(cancelThreshold - 20, 0f)
                        dragOffset = newOffset
                        onDragOffsetChange(newOffset)
                    })
            }) {
        Icon(
            imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicNone,
            contentDescription = contentRecordText,
            modifier = Modifier
                .size(25.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    if (permissionNeeded) {
        AudioPermissionRequest(showBottomSheet = true,
            onShowBottomSheetChange = { permissionNeeded = false },
            onRequestPermission = {
                launcher.launch(permission)
                permissionNeeded = false
            },
            onCancel = {
                onCancelRecording()
                permissionNeeded = false
            },
            closeText = closeText,
            allowAudioText = allowAudioText,
            enableAudioText = enableAudioText,
            cancelText = cancelText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPermissionRequest(
    showBottomSheet: Boolean,
    onShowBottomSheetChange: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    closeText: String,
    enableAudioText: String,
    allowAudioText: String,
    cancelText: String,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { false }, skipPartiallyExpanded = true
    )

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { onShowBottomSheetChange(false) },
            modifier = Modifier.height(375.dp),
            sheetState = sheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = closeText,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onShowBottomSheetChange(false) },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }) {
            PermissionContent(onCancel = onCancel, cancelText = cancelText) {
                Text(
                    text = enableAudioText,
                    modifier = Modifier.width(250.dp),
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRequestPermission) {
                    Text(text = allowAudioText)
                }
            }
        }
    }
}
