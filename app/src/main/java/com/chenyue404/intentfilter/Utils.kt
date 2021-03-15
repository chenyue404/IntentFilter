package com.chenyue404.intentfilter

import android.content.Context
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class Utils {
}

fun Long.timeToStr(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(this)

fun Int.dp2Px(context: Context): Int {
    return (this * context.resources.displayMetrics.density + 0.5f).toInt()
}

inline fun <reified T> fromJson(json: String?): T {
    return Gson().fromJson<T>(json, object : TypeToken<T>() {}.type)
}

fun getResourceIdByName(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "string", context.packageName)
}

fun View.visible(visibility: Boolean) {
    this.visibility = if (visibility) View.VISIBLE else View.GONE
}