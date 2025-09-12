package com.chenyue404.intentfilter

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        lateinit var gContext: App
        const val SPLIT_LETTER = ","
        const val PREF_NAME = "main_prefs"
        const val KEY_NAME = "main_prefs_key"
        const val KEY_SHOW_LOG_NAME = "key_show_log_name"
        const val KEY_SEND_BROADCAST = "key_send_broadcast"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        gContext = this
    }
}