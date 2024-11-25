package com.twnel.android_components.models

import com.twnel.android_components.ContentType
import java.util.UUID

abstract class AbstractMessage {
    var id: String = UUID.randomUUID().toString()
    var text: String = ""
    var author: String = ""
    var createdAt: Long = System.currentTimeMillis()
    var type: String = ContentType.TEXT
    var media: String = ""
    var companyId: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var conversationId: String = ""
    var pathFile: String = ""
    var aspectRatio: Float = 1f
    var isSent: Boolean = false
    var sendHour: Long = 0
    var deliveredHour: Long = 0
    var imageType: String = ""
    var audioType: String = ""
    var audioDuration: Int = 0
    var waveformData: String = ""
    var isSending: Boolean = false
    var showInfo: Boolean = false
}
