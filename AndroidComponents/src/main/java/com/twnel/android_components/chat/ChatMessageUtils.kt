package com.twnel.android_components.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.twnel.android_components.models.AbstractMessage

fun setWidthAndHeightImage(message: AbstractMessage): Pair<Dp, Dp> {
    var width = 300.dp
    var height = 300.dp
    if (message.aspectRatio > 1) {
        height /= message.aspectRatio
    } else if (message.aspectRatio < 1) {
        width *= message.aspectRatio
    }
    return Pair(width, height)
}

fun formatRecordingDuration(durationInSeconds: Int): String {
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

fun takePhoto(
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
