package com.chenyue404.intentfilter

import android.content.Context
import android.content.Intent
import android.view.View
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Locale

class Utils {
}

fun Long.timeToStr(): String =
    SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss.SSS", Locale.ROOT).format(this)

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