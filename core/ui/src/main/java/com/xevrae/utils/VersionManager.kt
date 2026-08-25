package com.xevrae.utils

import com.xevrae.ui.AppGlobalContext

object VersionManager {
    private var versionName: String? = null

    fun initialize() {
        if (versionName == null) {
            versionName =
                try {
                    val context = AppGlobalContext.get()
                    if (context != null) {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        pInfo.versionName
                    } else {
                        "1.0.0"
                    }
                } catch (_: Exception) {
                    "1.0.0"
                }
        }
    }

    fun getVersionName(): String = removeDevSuffix(versionName ?: "1.0.0")

    private fun removeDevSuffix(versionName: String): String {
        return if (versionName.endsWith("-dev")) {
            versionName.replace("-dev", "")
        } else {
            versionName
        }
    }
}