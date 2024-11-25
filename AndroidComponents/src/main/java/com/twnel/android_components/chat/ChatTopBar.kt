package com.twnel.android_components.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twnel.android_components.UIConstant
import com.twnel.android_components.utils.ImageTypeSource

@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    logo: Any,
    name: String,
    subtitle: String = "",
    imageType: String = UIConstant.EXTERNAL_IMAGE
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onBack()
        }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        ImageTypeSource(
            imageType = imageType,
            image = logo,
            description = "Company Logo",
            size = 40.dp,
            roundCorner = 8.dp
        )
        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontWeight = FontWeight.Thin,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}
