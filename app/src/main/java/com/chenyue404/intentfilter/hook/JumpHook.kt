package com.chenyue404.intentfilter.hook

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import com.chenyue404.intentfilter.App
import com.chenyue404.intentfilter.BuildConfig
import com.chenyue404.intentfilter.LogReceiver
import com.chenyue404.intentfilter.entity.LogEntity
import com.chenyue404.intentfilter.entity.RuleEntity
import com.chenyue404.intentfilter.fromJson
import com.google.gson.Gson
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class JumpHook : IXposedHookLoadPackage {

    private val PACKAGE_NAME = "android"
    private val TAG = "intentfilter--hook-"

    companion object {
        var ruleStr = ""
        var showLog = false
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        val classLoader = lpparam.classLoader

        if (packageName != PACKAGE_NAME) {
            return
        }

        hookCheckBroadcastFromSystem(classLoader)
        hookStartActivity(classLoader)

        when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.N,
            Build.VERSION_CODES.N_MR1 -> {
                XposedHelpers.findAndHookMethod(
                    "com.android.server.pm.PackageManagerService",
                    classLoader,
                    "queryIntentActivitiesInternal",
                    Intent::class.java,
                    String::class.java,
                    Int::class.java,
                    Int::class.java,
                    createCallback()
                )
            }
            Build.VERSION_CODES.O -> {
                XposedHelpers.findAndHookMethod(
                    "com.android.server.pm.PackageManagerService",
                    classLoader,
                    "queryIntentActivitiesInternal",
                    Intent::class.java,
                    String::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Boolean::class.java,
                    createCallback()
                )
            }
            Build.VERSION_CODES.O_MR1,
            Build.VERSION_CODES.P,
            Build.VERSION_CODES.Q -> {
                XposedHelpers.findAndHookMethod(
                    "com.android.server.pm.PackageManagerService",
                    classLoader,
                    "queryIntentActivitiesInternal",
                    Intent::class.java,
                    String::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Boolean::class.java,
                    Boolean::class.java,
                    createCallback()
                )
            }
            Build.VERSION_CODES.R -> {
                XposedHelpers.findAndHookMethod(
                    "com.android.server.pm.PackageManagerService",
                    classLoader,
                    "queryIntentActivitiesInternal",
                    Intent::class.java,
                    String::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Boolean::class.java,
                    Boolean::class.java,
                    createCallback()
                )
            }
        }
    }

    /**
     * ?????????????????????????????????????????????
     */
    private fun hookCheckBroadcastFromSystem(classLoader: ClassLoader) {
        val ProcessRecord =
            XposedHelpers.findClass("com.android.server.am.ProcessRecord", classLoader)
        XposedHelpers.findAndHookMethod(
            "com.android.server.am.ActivityManagerService", classLoader,
            "checkBroadcastFromSystem",
            Intent::class.java,
            ProcessRecord,
            String::class.java,
            Int::class.java,
            Boolean::class.java,
            List::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent: Intent = param.args[0] as Intent
                    if (intent.action == LogReceiver.ACTION) {
                        param.args[4] = true
                    }
                }
            }
        )
    }

    private fun getFilterCallingUidIndex() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            4
        } else {
            3
        }

    private fun createCallback(): XC_MethodHook {
        return object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent
                val intentAction = intent.action ?: ""
                val intentType = intent.type ?: ""
                val intentCompPackage = intent.component?.packageName ?: ""
                val dataString = intent.dataString ?: ""
                val list = param.result as List<ResolveInfo>
                if (list.isNullOrEmpty() || (intentAction == Intent.ACTION_MAIN && intentCompPackage != BuildConfig.APPLICATION_ID)) {
                    return
                }

                val filterCallingUid = (param.args[getFilterCallingUidIndex()] as Int).toString()
                val contextField = XposedHelpers.findFieldIfExists(
                    param.thisObject.javaClass,
                    "mContext"
                )
                val mContext = contextField[param.thisObject] as Context

                showLog = XSharedPreferences(
                    BuildConfig.APPLICATION_ID,
                    App.PREF_NAME
                ).getBoolean(App.KEY_SHOW_LOG_NAME, false)

                if (ruleStr.isEmpty()
                    || (!TextUtils.isEmpty(intentCompPackage) && intentCompPackage == BuildConfig.APPLICATION_ID)
                ) {
//                    log("ruleStr????????????=$ruleStr")
                    ruleStr = XSharedPreferences(
                        BuildConfig.APPLICATION_ID,
                        App.PREF_NAME
                    ).getString(App.KEY_NAME, "").toString()
                } else {
//                    log("ruleStr??????=$ruleStr")
                }
                if (ruleStr.contains("\"a\":")) {
                    ruleStr = App.EMPTY_STR
                    log("??????")
                }
                val ruleList = arrayListOf<RuleEntity>()
                if (ruleStr.isNotEmpty() && ruleStr != App.EMPTY_STR) {
                    ruleList.addAll(fromJson<ArrayList<RuleEntity>>(ruleStr))
                }
                val ruleIsEmpty =
                    ruleStr.isEmpty() || ruleStr == App.EMPTY_STR || ruleList.isNullOrEmpty()

                val activityList = list.map {
                    val activityInfo = it.activityInfo
                    val activityName = "${activityInfo.packageName}/${activityInfo.name}"
                    activityName
                }

                val indexList = hashSetOf<Int>()
                if (!ruleIsEmpty) {
                    for (ruleEntity in ruleList) {
                        val actionMatch = if (ruleEntity.actionKeywords.isEmpty()) true
                        else ruleEntity.actionBlack ==
                                ruleEntity.actionKeywords.split(App.SPLIT_LETTER)
                                    .any { intentAction.contains(it) }

                        val typeMatch = if (ruleEntity.typeKeywords.isEmpty()) true
                        else ruleEntity.typeBlack ==
                                ruleEntity.typeKeywords.split(App.SPLIT_LETTER)
                                    .any { intentType.contains(it) }

                        val dataStringMatch = if (ruleEntity.dataStringKeywords.isEmpty()) true
                        else ruleEntity.dataStringBlack ==
                                ruleEntity.dataStringKeywords.split(App.SPLIT_LETTER)
                                    .any { dataString.contains(it) }

                        val uidMatch = if (ruleEntity.uids.isEmpty()) true
                        else ruleEntity.uidBlack ==
                                ruleEntity.uids.split(App.SPLIT_LETTER)
                                    .any { filterCallingUid.contains(it) }
                        val needMatchActivity =
                            actionMatch && typeMatch && dataStringMatch && uidMatch

                        list.forEachIndexed { index, resolveInfo ->
                            val activityInfo = resolveInfo.activityInfo
                            var needFilterOut = false
                            if (activityInfo != null) {
                                val activityName =
                                    "${activityInfo.packageName}/${activityInfo.name}"
                                needFilterOut = needMatchActivity &&
                                        if (ruleEntity.activityKeywords.isEmpty()) true
                                        else ruleEntity.activityBlack ==
                                                ruleEntity.activityKeywords.split(App.SPLIT_LETTER)
                                                    .any { activityName.contains(it) }
                            }
                            if (needFilterOut) {
                                indexList.add(index)
                            }
                        }
                    }

                    if (indexList.isNotEmpty()) {
                        val mutableList = list.toMutableList()
                        indexList.sortedByDescending { it }.forEach {
                            mutableList.removeAt(it)
                        }
                        param.result = mutableList
                    }
                }

                log(
                    "intent=$intent\n" +
                            "uid=$filterCallingUid\n" +
                            "activity=$activityList\n" +
                            "blocked=$indexList"
                )

                if (list.isNotEmpty()
                    && (intentAction.isNotEmpty() || dataString.isNotEmpty())
                    && (intentAction.isEmpty() || intentAction != Intent.ACTION_MAIN)
                ) {
                    Handler(mContext.mainLooper).post {
                        mContext.sendBroadcast(Intent().apply {
                            action = LogReceiver.ACTION
                            putExtra(
                                LogReceiver.EXTRA_KEY, Gson().toJson(
                                    LogEntity(
                                        time = System.currentTimeMillis(),
                                        uid = filterCallingUid,
                                        action = intentAction,
                                        type = intentType,
                                        dataString = dataString,
                                        activities = activityList.joinToString(separator = App.SPLIT_LETTER),
                                        blockIndexes = indexList.joinToString(separator = App.SPLIT_LETTER)
                                    )
                                )
                            )
                        })
                    }
                }
            }
        }
    }

    private fun hookStartActivity(classLoader: ClassLoader) {
    }

    private fun log(str: String) {
        if (!BuildConfig.DEBUG && !showLog) return
        XposedBridge.log("$TAG-$str")
    }
}