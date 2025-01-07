package com.twnel.android_components.chat

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

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
