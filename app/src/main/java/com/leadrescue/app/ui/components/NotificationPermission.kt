package com.leadrescue.app.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.leadrescue.app.reminders.Notifications

/** Current notification state plus a way to ask the user to turn notifications on. */
class NotificationAccess(val enabled: Boolean, val request: () -> Unit)

/**
 * Tracks whether reminders can be shown (re-checked whenever the app comes back to the screen).
 * request() shows Android's permission dialog; if Android won't show it any more (the user
 * declined twice) it opens this app's notification settings instead.
 */
@Composable
fun rememberNotificationAccess(): NotificationAccess {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(Notifications.canNotify(context)) }

    LifecycleResumeEffect(Unit) {
        enabled = Notifications.canNotify(context)
        onPauseOrDispose { }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        enabled = Notifications.canNotify(context)
        if (!granted || !enabled) Intents.openNotificationSettings(context)
    }

    return NotificationAccess(enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !enabled) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Intents.openNotificationSettings(context)
        }
    }
}
