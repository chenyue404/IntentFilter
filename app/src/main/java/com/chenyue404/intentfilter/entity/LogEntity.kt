package com.chenyue404.intentfilter.entity

import com.chenyue404.intentfilter.App
import com.chenyue404.intentfilter.queryActivityInfo
import com.chenyue404.intentfilter.queryAppInfo

data class LogEntity(
    val time: Long,
    val from: String = "",
    val action: String = "",
    val type: String = "",
    val dataString: String = "",
    val activities: String = "",
    val blockIndexes: String = ""
) {
    lateinit var fromInfoList: List<Pair<String, BasicInfo>>
    lateinit var activityInfoList: List<Pair<String, BasicInfo>>
    lateinit var blockList: List<String>

    fun processData() {
        val fromList = from.split(App.SPLIT_LETTER)
        fromInfoList = buildInfoList(fromList)

        val activityList = activities.split(App.SPLIT_LETTER)
        activityInfoList = buildInfoList(activityList)

        val indexList = blockIndexes.split(App.SPLIT_LETTER)
        blockList = indexList.mapNotNull {
            it.toIntOrNull()?.let { index -> activityList.getOrNull(index) }
        }
    }

    private fun buildInfoList(list: List<String>): List<Pair<String, BasicInfo>> {
        val newList: MutableList<Pair<String, BasicInfo>> = mutableListOf()
        val lastPkg = mutableListOf<String>()
        list.forEach {
            val isActivity = it.contains("/")
            if (isActivity) {
                val pkg = it.split("/").first()
                if (lastPkg.firstOrNull() == pkg) {
                    if (lastPkg.size == 1) {
                        newList.add(newList.size - 1, pkg to queryAppInfo(pkg))
                    }
                } else {
                    lastPkg.clear()
                }
                lastPkg.add(pkg)
                newList.add(it to queryActivityInfo(it))
            } else {
                newList.add(it to queryAppInfo(it))
            }
        }
        return newList
    }

}
