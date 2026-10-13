package com.leadrescue.app

import android.content.Context

/** Reads the installed version name without enabling the BuildConfig feature. */
object BuildConfigInfo {
    fun versionName(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    } catch (_: Exception) {
        ""
    }
}
