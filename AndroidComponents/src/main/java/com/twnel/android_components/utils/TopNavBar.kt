package com.twnel.android_components.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopNavBar(
    messageServiceConnected: Boolean,
    navigateToProfile: () -> Unit,
    text: String,
    stringConnecting: String,
    modifier: Modifier = Modifier,
) {
    Box {
        Column(
            modifier = Modifier.padding(bottom = 0.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 0.dp)

            ) {
                Text(
                    text = text, fontSize = 30.sp, modifier = modifier.padding(
                        start = 6.dp, end = 6.dp, top = 4.dp, bottom = 0.dp
                    ), fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .clickable { navigateToProfile() }, contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person, contentDescription = "Profile"
                    )
                }
            }
            Box {
                ShowMessageConnecting(!messageServiceConnected, stringConnecting)
            }
        }
    }
}

@Composable
fun ShowMessageConnecting(mqttConnected: Boolean, stringConnecting: String) {
    Row {
        Text(
            text = if (mqttConnected) "$stringConnecting..." else "",
            fontSize = 12.sp,
            style = LocalTextStyle.current.copy(lineHeight = 14.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            textAlign = TextAlign.Center
        )
    }
}
