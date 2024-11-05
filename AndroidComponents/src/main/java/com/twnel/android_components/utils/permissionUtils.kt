package com.twnel.android_components.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat


fun checkMediaPermissions(
    permissions: Map<String, Boolean>, context: Context, onPass: () -> Unit = {}
) {
    if (permissions.getOrDefault(
            Manifest.permission.READ_MEDIA_IMAGES, false
        ) || permissions.getOrDefault(
            Manifest.permission.READ_EXTERNAL_STORAGE, false
        ) || permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)
    ) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onPass()
        }
    }
}

@Composable
fun PermissionContent(
    onCancel: () -> Unit, cancelText: String, content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Cancel, contentDescription = cancelText
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                content()
            }
        }
    }
}