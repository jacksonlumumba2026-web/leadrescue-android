package com.leadrescue.app.data

import com.leadrescue.app.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow

/**
 * The single place screens go to read and change leads.
 * Every change also updates that lead's reminder, so reminders never get out of sync.
 */
class LeadRepository(
    private val dao: LeadDao,
    private val reminders: ReminderScheduler,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<Lead>> = dao.observeAll()

    fun observe(id: Long): Flow<Lead?> = dao.observeById(id)

    suspend fun get(id: Long): Lead? = dao.getById(id)

    suspend fun openWithFollowUp(): List<Lead> = dao.getOpenWithFollowUp()

    suspend fun add(lead: Lead): Long {
        val now = clock()
        val id = dao.insert(lead.copy(id = 0, createdAt = now, updatedAt = now))
        reminders.sync(lead.copy(id = id))
        return id
    }

    suspend fun save(lead: Lead) {
        val updated = lead.copy(updatedAt = clock())
        dao.update(updated)
        reminders.sync(updated)
    }

    suspend fun setStatus(lead: Lead, status: LeadStatus) = save(lead.copy(status = status))

    suspend fun setFollowUp(lead: Lead, followUpAt: Long?) = save(lead.copy(followUpAt = followUpAt))

    /** Called after the user opened WhatsApp and confirmed they sent the message. */
    suspend fun markContacted(lead: Lead) {
        val status = if (lead.status == LeadStatus.NEW) LeadStatus.CONTACTED else lead.status
        save(lead.copy(status = status, lastContactedAt = clock()))
    }

    suspend fun delete(lead: Lead) {
        dao.delete(lead)
        reminders.cancel(lead.id)
    }

    /** Re-creates every pending reminder, e.g. after the app was updated. Safe to call repeatedly. */
    suspend fun resyncReminders() {
        dao.getOpenWithFollowUp().forEach { reminders.sync(it) }
    }
}
