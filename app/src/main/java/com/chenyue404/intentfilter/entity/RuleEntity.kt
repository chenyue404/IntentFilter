package com.chenyue404.intentfilter.entity

data class RuleEntity(
    var dataStringKeywords: String = "",
    var activityKeywords: String = "",
    var uids: String = "",
    var dataStringBlack: Boolean = true,
    var activityBlack: Boolean = true,
    var uidBlack: Boolean = true,
)
