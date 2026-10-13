package com.leadrescue.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Small helpers for opening other apps. Each returns false if nothing on the phone could handle it. */
object Intents {

    fun openUrl(context: Context, url: String): Boolean = start(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    /** Opens the phone dialer with the number filled in. The user still taps call. */
    fun dial(context: Context, phone: String): Boolean = start(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))

    fun openNotificationSettings(context: Context): Boolean =
        start(
            context,
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
        ) || openAppSettings(context)

    /** App info page: on Samsung this is where Battery -> Unrestricted lives. */
    fun openAppSettings(context: Context): Boolean =
        start(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))

    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
