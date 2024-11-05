package com.twnel.android_components.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextInput(
    messageText: String,
    placeholder: String? = null,
    setMessageText: (String) -> Unit,
    addMessage: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {

        OutlinedTextField(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(0.9f),
            value = messageText,
            onValueChange = { setMessageText(it) },
            placeholder = { Text(text = placeholder ?: "") },
        )
        IconButton(onClick = {
            addMessage()
        }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}
