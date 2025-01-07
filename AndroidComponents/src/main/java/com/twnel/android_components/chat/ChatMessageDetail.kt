package com.twnel.android_components.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twnel.android_components.UIConstant
import com.twnel.android_components.utils.ImageTypeSource

import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessageDetail(
    author: String,
    messageText: String,
    messageSendHour: Long,
    messageDeliveredHour: Long,
    color: Color,
    sentString: String,
    receivedString: String,
    closeModal: () -> Unit,
    image: Any = "",
    imageType: String = UIConstant.ICON_IMAGE
) {
    ModalBottomSheet(onDismissRequest = { closeModal() }) {
        Card(
            elevation = CardDefaults.cardElevation(), modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val (imgSrc: Any, roundCorner) = if (image != "") image to 16.dp else Icons.Default.SentimentSatisfiedAlt to 8.dp
                    ImageTypeSource(
                        imageType = imageType,
                        image = imgSrc,
                        description = "User Icon",
                        size = 50.dp,
                        roundCorner = roundCorner,
                        backgroundColor = color
                    )
                }
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = author,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = messageText)
                    Row {
                        Text(
                            text = "$sentString ", fontWeight = FontWeight.Bold, fontSize = 12.sp
                        )
                        Text(
                            text = SimpleDateFormat(
                                UIConstant.DATE_HOUR_FORMAT, Locale.getDefault()
                            ).format(
                                messageSendHour
                            ),
                            fontSize = 12.sp,
                        )
                    }
                    Row {
                        Text(
                            text = "$receivedString ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = SimpleDateFormat(
                                UIConstant.DATE_HOUR_FORMAT, Locale.getDefault()
                            ).format(
                                messageDeliveredHour
                            ),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
