package com.leadrescue.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.leadrescue.app.LeadRescueApp

/** Runs at a lead's follow-up time and shows the reminder notification. */
class FollowUpWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val leadId = inputData.getLong(KEY_LEAD_ID, -1)
        val scheduledFor = inputData.getLong(KEY_FOLLOW_UP_AT, -1)
        val container = (applicationContext as LeadRescueApp).container
        val lead = container.repository.get(leadId) ?: return Result.success()

        // Skip if the lead was closed or rescheduled after this reminder was created.
        if (!lead.status.isOpen || lead.followUpAt != scheduledFor) return Result.success()

        Notifications.showFollowUp(applicationContext, lead, container.settings.businessName.value)
        return Result.success()
    }

    companion object {
        const val KEY_LEAD_ID = "lead_id"
        const val KEY_FOLLOW_UP_AT = "follow_up_at"
    }
}
