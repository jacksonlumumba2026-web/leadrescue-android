package com.leadrescue.app.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.leadrescue.app.data.Lead
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Schedules reminders with WorkManager.
 *
 * Why WorkManager: it needs no special "exact alarm" permission, it survives phone restarts
 * and app updates, and it respects battery saving. The trade-off is that Android may deliver a
 * reminder a few minutes late when the phone is idle. For follow-ups that is fine.
 */
class ReminderScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    /** Makes the lead's reminder match the lead: scheduled if it is open with a future follow-up, otherwise removed. */
    fun sync(lead: Lead, now: Long = System.currentTimeMillis()) {
        val followUpAt = lead.followUpAt
        if (!lead.status.isOpen || followUpAt == null || followUpAt <= now) {
            cancel(lead.id)
            return
        }
        val request = OneTimeWorkRequestBuilder<FollowUpWorker>()
            .setInitialDelay(followUpAt - now, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    FollowUpWorker.KEY_LEAD_ID to lead.id,
                    FollowUpWorker.KEY_FOLLOW_UP_AT to followUpAt,
                )
            )
            .addTag(TAG_FOLLOW_UP)
            .build()
        workManager.enqueueUniqueWork(workName(lead.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(leadId: Long) {
        workManager.cancelUniqueWork(workName(leadId))
    }

    /** A once-a-day summary around 8:00 AM of follow-ups due today and overdue. */
    fun scheduleDailyDigest() {
        val request = PeriodicWorkRequestBuilder<DailyDigestWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNext(DIGEST_TIME), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(DIGEST_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun millisUntilNext(time: LocalTime): Long {
        val now = LocalDateTime.now()
        var next = LocalDateTime.of(LocalDate.now(), time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    private fun workName(leadId: Long) = "follow-up-$leadId"

    private companion object {
        const val TAG_FOLLOW_UP = "follow-up"
        const val DIGEST_WORK = "daily-digest"
        val DIGEST_TIME: LocalTime = LocalTime.of(8, 0)
    }
}
