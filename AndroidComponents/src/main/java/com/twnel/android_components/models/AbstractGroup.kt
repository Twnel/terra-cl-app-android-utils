package com.twnel.android_components.models

abstract class AbstractGroup {
    var id = ""
    var name = ""
    var subject = ""
    var image = ""
    var tags = listOf<String>()
    var country = ""
    var company = ""
}
