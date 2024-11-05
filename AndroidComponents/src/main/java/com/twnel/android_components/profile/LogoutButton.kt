package com.twnel.android_components.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme as Material3Theme

@Composable
fun LogoutButton(
    text: String,
    description: String,
    onLogout: () -> Unit,
    logoutText: String,
    logoutMessage: String,
    cancelText: String,
    modifier: Modifier = Modifier
) {
    var showConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = { showConfirmation = true }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.PowerSettingsNew, contentDescription = description)
        Text(text = text, style = Material3Theme.typography.bodyLarge)
    }

    AnimatedVisibility(
        visible = showConfirmation,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        LogoutConfirmationDialog(
            onConfirm = {
                onLogout()
                showConfirmation = false
            },
            onDismiss = { showConfirmation = false },
            logoutText = logoutText,
            cancelText = cancelText,
            logoutMessage = logoutMessage
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    logoutText: String,
    logoutMessage: String,
    cancelText: String
) {
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            logoutText, style = Material3Theme.typography.bodyLarge, fontWeight = FontWeight.Bold
        )
    }, text = {
        Text(
            logoutMessage, style = Material3Theme.typography.bodyMedium
        )
    }, confirmButton = {
        Button(
            onClick = onConfirm
        ) {
            Text(logoutText)
        }
    }, dismissButton = {
        OutlinedButton(onClick = onDismiss) {
            Text(cancelText)
        }
    })
}
