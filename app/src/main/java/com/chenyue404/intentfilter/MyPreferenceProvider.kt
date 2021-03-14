package com.chenyue404.intentfilter

import com.crossbowffs.remotepreferences.RemotePreferenceProvider

class MyPreferenceProvider :
    RemotePreferenceProvider(
        App.gContext.getString(R.string.provider_authority),
        arrayOf(PREF_NAME)
    ) {
    companion object {
        val PREF_NAME = "main_prefs"
        val KEY_NAME = "main_prefs_key"
        val EMPTY_STR = "EMPTY_STR"
    }
}