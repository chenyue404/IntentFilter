package com.chenyue404.intentfilter

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        lateinit var gContext: App
        val SPLIT_LETTER = ","
        val TAG = "intentfilter--app-"
        val PREF_NAME = "main_prefs"
        val KEY_NAME = "main_prefs_key"
        val EMPTY_STR = "EMPTY_STR"
        val KEY_SHOW_LOG_NAME = "key_show_log_name"
        val KEY_SEND_BROADCAST = "key_send_broadcast"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        gContext = this
    }
}