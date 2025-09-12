package com.chenyue404.intentfilter.entity

import com.chenyue404.intentfilter.App

data class RuleEntity(
    var actionKeywords: String = "",
    var typeKeywords: String = "",
    var dataStringKeywords: String = "",
    var activityKeywords: String = "",
    var from: String = "",

    var actionBlack: Boolean = true,
    var typeBlack: Boolean = true,
    var dataStringBlack: Boolean = true,
    var activityBlack: Boolean = true,
    var fromBlack: Boolean = true,

    var actionKeywordsList: List<String>? = null,
    var typeKeywordsList: List<String>? = null,
    var dataStringKeywordsList: List<String>? = null,
    var activityKeywordsList: List<String>? = null,
    var fromList: List<String>? = null,
) {
    fun fillWordsList() {
        actionKeywordsList = actionKeywords.split(App.SPLIT_LETTER)
        typeKeywordsList = typeKeywords.split(App.SPLIT_LETTER)
        dataStringKeywordsList = dataStringKeywords.split(App.SPLIT_LETTER)
        activityKeywordsList = activityKeywords.split(App.SPLIT_LETTER)
        fromList = from.split(App.SPLIT_LETTER)
    }
}