package com.leadrescue.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.leadrescue.app.LeadRescueApp
import com.leadrescue.app.util.DueState
import com.leadrescue.app.util.FollowUpDates

/** Morning summary: "You have 3 follow-ups today and 2 overdue." Silent when there is nothing to do. */
class DailyDigestWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as LeadRescueApp).container.repository
        val now = System.currentTimeMillis()
        val open = repository.openWithFollowUp()
        val dueToday = open.count { FollowUpDates.dueState(it.followUpAt, now) == DueState.TODAY }
        val overdue = open.count { FollowUpDates.dueState(it.followUpAt, now) == DueState.OVERDUE }
        if (dueToday + overdue > 0) {
            Notifications.showDigest(applicationContext, dueToday, overdue)
        }
        // Also repair any reminder that may have been lost (e.g. after an update).
        repository.resyncReminders()
        return Result.success()
    }
}
