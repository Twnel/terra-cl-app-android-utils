package com.twnel.android_components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em


@Composable
fun ProfileName(
    name: String,
    onNameChange: (String) -> Unit,
    placeholderText: String,
    saveDescription: String,
    editDescription: String,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(name) { mutableStateOf(name) }

    val textStyle = MaterialTheme.typography.headlineSmall.copy(
        textAlign = TextAlign.Center, lineHeight = 0.em, platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ), lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.Both
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(48.dp))

        if (isEditing) {
            TextField(
                value = editedName,
                onValueChange = { editedName = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onNameChange(editedName.trim())
                    isEditing = false
                }),
                placeholder = {
                    Text(
                        placeholderText,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                textStyle = textStyle,
                modifier = Modifier
                    .weight(1f)
                    .width(IntrinsicSize.Min)
                    .background(Color.Transparent),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )
            IconButton(onClick = {
                onNameChange(editedName.trim())
                isEditing = false
            }) {
                Icon(Icons.Default.Check, contentDescription = saveDescription)
            }
        } else {
            Text(text = name.ifBlank { placeholderText },
                style = textStyle,
                color = if (name.isBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isEditing = true }
                    .padding(vertical = 16.dp))
            IconButton(onClick = { isEditing = true }) {
                Icon(
                    Icons.Default.Edit, contentDescription = editDescription
                )
            }
        }
    }
}

