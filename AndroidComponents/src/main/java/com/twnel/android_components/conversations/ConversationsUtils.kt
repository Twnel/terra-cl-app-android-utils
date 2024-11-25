package com.twnel.android_components.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twnel.android_components.UIConstant
import com.twnel.android_components.models.AbstractGroup
import com.twnel.android_components.utils.ImageTypeSource

@Composable
fun ShowInfoLine(companyName: String, companiesSize: Int) {
    if (companiesSize > 1) {
        Text(text = companyName, fontWeight = FontWeight.Bold)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary,
            thickness = 1.dp
        )
    }
}

@Composable
private fun IconMessageConversation(
    iconMessage: String, textMessage: String
) {

    if (iconMessage.isNotEmpty()) {
        Text(
            text = iconMessage,
            modifier = Modifier.padding(0.dp),
        )
        Text(
            text = textMessage,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 2,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(bottom = 0.dp, top = 2.dp)
        )
    } else {
        Text(
            text = textMessage,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 2,
            fontSize = 15.sp,
            overflow = TextOverflow.Ellipsis,
            fontStyle = FontStyle.Italic,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
    }

}

@Composable
fun Conversation(
    modifier: Modifier = Modifier,
    isBot: Boolean = false,
    textMessage: String = "",
    lastMessageHour: String = "",
    numberMessageWithoutRead: Int = 0,
    imageType: String = UIConstant.ICON_IMAGE,
    image: Any = "",
    title: String = "",
    iconMessage: String = "",
) {
    Row(
        modifier = modifier.padding(8.dp)
    ) {
        ImageTypeSource(
            imageType = imageType,
            image = image,
            description = "Company Logo",
            size = 60.dp,
            iconSize = if (imageType == UIConstant.ICON_IMAGE && isBot) 50.dp else null,
            roundCorner = 16.dp
        )
        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (!isBot) {
                    Text(
                        text = lastMessageHour, modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconMessageConversation(iconMessage, textMessage)
                }
                if (!isBot && numberMessageWithoutRead > 0) {
                    Text(
                        text = numberMessageWithoutRead.toString(),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(start = 4.dp, end = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun getConversationInfo(
    companyLogo: String,
    conversationId: String,
    getGroupById: (String) -> AbstractGroup?,
    stringTeam: String
): Triple<String, String, Boolean> {
    var title = stringTeam
    var image = companyLogo
    var isGroup = false
    if (conversationId != "main") {
        val group = getGroupById(conversationId)
        if (group != null) {
            title = group.name
        }
        if (group != null && group.image.isNotEmpty()) {
            image = group.image
        }
        isGroup = true
    }
    return Triple(title, image, isGroup)
}

@Composable
fun ShowChatBot(
    companyId: String,
    companyChatBotsCount: Int,
    navigateToChatbot: (String) -> Unit = {},
    stringConversationsBotMessage: String,
    stringTools: String,
    drawableToolsId: Int
) {
    if (companyChatBotsCount > 0) {
        Conversation(
            modifier = Modifier.clickable(onClick = {
                navigateToChatbot(companyId)
            }),
            isBot = true,
            textMessage = stringConversationsBotMessage,
            title = stringTools,
            imageType = UIConstant.LOCAL_IMAGE,
            image = painterResource(id = drawableToolsId)
        )
    }
}
