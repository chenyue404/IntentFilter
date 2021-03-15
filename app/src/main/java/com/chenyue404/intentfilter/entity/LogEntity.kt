package com.chenyue404.intentfilter.entity

data class LogEntity(
    val time: Long,
    val uid: String = "",
    val action: String = "",
    val type: String = "",
    val dataString: String = "",
    val activities: String = "",
    val blockIndexes: String = ""
)
