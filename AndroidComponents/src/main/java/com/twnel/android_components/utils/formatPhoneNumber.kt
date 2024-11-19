package com.twnel.android_components.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil

fun formatPhoneNumber(phoneNumber: String): String {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    return try {
        val numberProto = phoneNumberUtil.parse(phoneNumber, "")
        phoneNumberUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    } catch (e: Exception) {
        phoneNumber
    }
}
