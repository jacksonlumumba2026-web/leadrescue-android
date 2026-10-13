package com.leadrescue.app.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leadrescue.app.MainActivity
import com.leadrescue.app.R
import com.leadrescue.app.data.Lead
import com.leadrescue.app.util.WhatsAppMessages

object Notifications {

    const val CHANNEL_FOLLOW_UPS = "follow_ups"
    private const val DIGEST_NOTIFICATION_ID = 1
    private const val BRAND_COLOR = 0xFF0E7C66.toInt()

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_FOLLOW_UPS,
            context.getString(R.string.channel_followups_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.channel_followups_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** True when the app is allowed to show notifications (permission granted and not blocked in Settings). */
    fun canNotify(context: Context): Boolean {
        val permissionOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permissionOk && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showFollowUp(context: Context, lead: Lead, businessName: String) {
        val message = WhatsAppMessages.build(lead.name, lead.product, lead.status, lead.valueKsh, businessName)
        val whatsAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(WhatsAppMessages.link(lead.phone, message)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val whatsApp = PendingIntent.getActivity(
            context,
            requestCode(lead.id, 1),
            whatsAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FOLLOW_UPS)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle("Follow up with ${lead.name}")
            .setContentText("${lead.product} · ${lead.status.label}. Tap to open the lead.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${lead.name} asked about ${lead.product}. A quick message now can win this sale."
                )
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openLeadIntent(context, lead.id))
            .addAction(0, "Open WhatsApp", whatsApp)
            .build()
        post(context, requestCode(lead.id, 0), notification)
    }

    fun showDigest(context: Context, dueToday: Int, overdue: Int) {
        val parts = buildList {
            if (dueToday > 0) add("$dueToday due today")
            if (overdue > 0) add("$overdue overdue")
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_FOLLOW_UPS)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle("Today's follow-ups")
            .setContentText(parts.joinToString(" · ") + ". Don't let these customers go cold.")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openLeadIntent(context, null))
            .build()
        post(context, DIGEST_NOTIFICATION_ID, notification)
    }

    private fun openLeadIntent(context: Context, leadId: Long?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (leadId != null) intent.putExtra(MainActivity.EXTRA_LEAD_ID, leadId)
        return PendingIntent.getActivity(
            context,
            if (leadId != null) requestCode(leadId, 2) else DIGEST_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("MissingPermission") // checked by canNotify()
    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        if (!canNotify(context)) return
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission was revoked between the check and the call; nothing else to do.
        }
    }

    /** Distinct ids per lead and purpose, kept clear of the digest id. */
    private fun requestCode(leadId: Long, kind: Int): Int = (leadId * 4 + kind + 10).toInt()
}
