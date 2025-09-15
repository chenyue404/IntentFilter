package com.chenyue404.intentfilter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import com.chenyue404.intentfilter.entity.BasicInfo
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Locale

fun Long.timeToStr(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(this)

fun Int.dp2Px(context: Context): Int {
    return (this * context.resources.displayMetrics.density + 0.5f).toInt()
}

fun getResourceIdByName(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "string", context.packageName)
}

fun View.visible(visibility: Boolean) {
    this.visibility = if (visibility) View.VISIBLE else View.GONE
}

fun Intent.transToStr(): String {
    val jsonObject = JsonObject().apply {
        addProperty("to", component?.className ?: "null")
        addProperty("action", action)
        addProperty("clipData", clipData.toString())
        addProperty("flags", flags)
        addProperty("dataString", dataString)
        addProperty("type", type)
        addProperty("componentName", component.toString())
        addProperty("scheme", scheme)
        addProperty("package", `package`)
        addProperty("categories", categories?.joinToString() ?: "null")
        extras?.let { bundle ->
            if (!bundle.isEmpty) {
                val jsonArray = JsonArray()
                bundle.keySet().forEach {
                    val value = bundle.get(it)
                    value?.run {
                        jsonArray.add(JsonObject().apply {
                            addProperty("key", it)
                            addProperty("value", value.toString())
                            addProperty("class", value.javaClass.name)
                        })
                    }

                }
                add("intentExtras", jsonArray)
            }
        }
    }

    return jsonObject.toString()
}

private val pkgManager by lazy {
    App.gContext.packageManager
}

private val appInfoCache = hashMapOf<String, BasicInfo>()
fun queryAppInfo(pkg: String): BasicInfo {
    val info = appInfoCache.get(pkg)
    if (info != null) {
        return info
    }
    val appInfo = pkgManager.getApplicationInfo(pkg, 0)
    with(
        BasicInfo(
            pkgManager.getApplicationLabel(appInfo).toString(),
            pkgManager.getApplicationIcon(appInfo)
        )
    ) {
        appInfoCache.put(pkg, this)
        return this
    }
}

fun queryActivityInfo(activityStr: String): BasicInfo {
    val info = appInfoCache.get(activityStr)
    if (info != null) {
        return info
    }
    with(activityStr.split("/").let {
        pkgManager.getActivityInfo(ComponentName(it[0], it[1]), 0)
    }.let {
        BasicInfo(
            it.loadLabel(pkgManager).toString(),
            it.loadIcon(pkgManager)
        )
    }) {
        appInfoCache.put(activityStr, this)
        return this
    }
}