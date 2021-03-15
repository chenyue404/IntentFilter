package com.chenyue404.intentfilter.hook

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import com.chenyue404.intentfilter.*
import com.chenyue404.intentfilter.entity.LogEntity
import com.chenyue404.intentfilter.entity.RuleEntity
import com.crossbowffs.remotepreferences.RemotePreferences
import com.google.gson.Gson
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class JumpHook : IXposedHookLoadPackage {

    private val PACKAGE_NAME = "android"
    private val TAG = "intentfilter--hook-"

    companion object {
        var ruleStr = ""
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
     * 解除系统不能发自定义广播的限制
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

    private fun createCallback(): XC_MethodHook {
        return object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent
                if (intent.action == null || intent.action != Intent.ACTION_VIEW) return
                val dataString = intent.dataString.toString()
                val intentPackage = if (intent.`package` != null) intent.`package` as String else ""
                val filterCallingUid = (param.args[3] as Int).toString()
                val list = param.result as List<ResolveInfo>
                val contextField = XposedHelpers.findFieldIfExists(
                    param.thisObject.javaClass,
                    "mContext"
                )
                val mContext = contextField[param.thisObject] as Context
                val myContext = mContext.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
                )
                val handlerField = XposedHelpers.findFieldIfExists(
                    param.thisObject.javaClass,
                    "mHandler"
                )
                val providerAuthority = myContext.getString(
                    getResourceIdByName(
                        myContext,
                        "provider_authority"
                    )
                )
                if (ruleStr.isEmpty()
                    || (!TextUtils.isEmpty(intentPackage) && intentPackage == BuildConfig.APPLICATION_ID)
                ) {
                    val mHandler = handlerField[param.thisObject] as Handler

                    log("$TAG ruleStr读取之前=$ruleStr")
                    mHandler.post {
                        ruleStr = RemotePreferences(
                            mContext,
                            providerAuthority,
                            MyPreferenceProvider.PREF_NAME
                        ).getString(MyPreferenceProvider.KEY_NAME, "").toString()
                        log("$TAG ruleStr读取=$ruleStr")
                    }
                } else {
                    log("$TAG ruleStr有值=$ruleStr")
                }
                val ruleList = arrayListOf<RuleEntity>()
                if (ruleStr.isNotEmpty() && ruleStr != MyPreferenceProvider.EMPTY_STR) {
                    ruleList.addAll(fromJson<ArrayList<RuleEntity>>(ruleStr))
                }
                val needActivityMatch = ruleList.any { ruleEntity ->
                    val dataStringMatch = (ruleEntity.dataStringKeywords.isEmpty() ||
                            ruleEntity.dataStringKeywords.split(App.SPLIT_LETTER).any {
                                dataString.contains(it)
                            }) && ruleEntity.dataStringBlack
                    val uidMatch =
                        (ruleEntity.uids.isEmpty() || ruleEntity.uids.split(App.SPLIT_LETTER)
                            .contains(filterCallingUid)) && ruleEntity.uidBlack
                    dataStringMatch && uidMatch
                }

                val indexList = arrayListOf<Int>()
                val activityList = arrayListOf<String>()
                val newList = arrayListOf<ResolveInfo>()
                list.forEachIndexed { index, resolveInfo ->
                    val activityInfo = resolveInfo.activityInfo
                    val packageName = activityInfo.packageName.toString()
                    val name = activityInfo.name.toString()
                    var needFilterOut = false
                    if (activityInfo != null) {
                        val activityStr = "$packageName/$name"
                        activityList.add(activityStr)
                        needFilterOut = needActivityMatch && ruleList.any { ruleEntity ->
                            ruleEntity.activityKeywords.split(App.SPLIT_LETTER).any {
                                activityStr.contains(it)
                            }
                        }
                    }
                    if (needFilterOut) {
                        indexList.add(index)
                    } else {
                        newList.add(resolveInfo)
                    }
                }
                log(
                    "intent=$intent\n" +
                            "uid=$filterCallingUid\n" +
                            "activity=$activityList\n" +
                            "blocked=$indexList"
                )

                mContext.sendBroadcast(Intent().apply {
                    action = LogReceiver.ACTION
                    putExtra(
                        LogReceiver.EXTRA_KEY, Gson().toJson(
                            LogEntity(
                                System.currentTimeMillis(),
                                filterCallingUid,
                                dataString,
                                activityList.joinToString(separator = App.SPLIT_LETTER),
                                indexList.joinToString(separator = App.SPLIT_LETTER)
                            )
                        )
                    )
                })

                if (indexList.isNotEmpty()) {
                    param.result = newList
                }
            }
        }
    }

    private fun hookStartActivity(classLoader: ClassLoader) {
    }

    private fun log(str: String) {
        if (!BuildConfig.DEBUG) return
        XposedBridge.log("$TAG-$str")
    }
}