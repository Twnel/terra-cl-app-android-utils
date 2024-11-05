package com.twnel.android_components.profile


import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme as Material3Theme

@Composable
fun OptionPicker(
    label: String,
    icon: @Composable () -> Unit,
    options: Map<String, String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    supportingText: String? = null,
    dialogTitle: String = "",
    cancelText: String,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = { showDialog = true }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            icon() // Leading icon
            Column {
                Text(
                    text = label, style = Material3Theme.typography.bodyLarge
                ) // Headline
                Text(
                    text = selectedOption,
                    style = Material3Theme.typography.bodyMedium,
                    color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) // Selected option with lower contrast
            }
        }
        if (showDialog) {
            AlertDialog(onDismissRequest = { showDialog = false },
                title = { Text(text = dialogTitle, style = Material3Theme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.padding(0.dp)) {
                        if (supportingText != null) {
                            Text(
                                text = supportingText, style = Material3Theme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        options.forEach { (key, option) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(selected = option == selectedOption, onClick = {
                                    onOptionSelected(key)
                                    showDialog = false
                                })
                                Text(text = option, style = Material3Theme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text(cancelText)
                    }
                })
        }
    }
}
