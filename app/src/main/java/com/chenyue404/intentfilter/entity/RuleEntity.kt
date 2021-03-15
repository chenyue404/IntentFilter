package com.chenyue404.intentfilter.entity

data class RuleEntity(
    var actionKeywords: String = "",
    var typeKeywords: String = "",
    var dataStringKeywords: String = "",
    var activityKeywords: String = "",
    var uids: String = "",

    var actionBlack: Boolean = true,
    var typeBlack: Boolean = true,
    var dataStringBlack: Boolean = true,
    var activityBlack: Boolean = true,
    var uidBlack: Boolean = true,
)
