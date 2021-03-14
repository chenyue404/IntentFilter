package com.chenyue404.intentfilter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chenyue404.intentfilter.entity.LogEntity
import com.google.gson.Gson

class LogReceiver(val block: (LogEntity) -> Unit) : BroadcastReceiver() {
    companion object {
        val ACTION = BuildConfig.APPLICATION_ID + "LOG_INTENT"
        val EXTRA_KEY = "extra_key"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logEntity = Gson().fromJson(intent.getStringExtra(EXTRA_KEY), LogEntity::class.java)
        block(logEntity)
    }
}