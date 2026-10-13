package com.leadrescue.app.ui

import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadSource
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.ui.viewmodel.DashboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStateTest {

    private val day = 24L * 60 * 60 * 1000

    private fun lead(id: Long, status: LeadStatus, followUpAt: Long?, value: Long? = null) = Lead(
        id = id, name = "Lead $id", phone = "+254712345678", product = "Item", source = LeadSource.OTHER,
        status = status, followUpAt = followUpAt, valueKsh = value, createdAt = 0, updatedAt = 0,
    )

    @Test
    fun `dashboard numbers`() {
        val now = System.currentTimeMillis()
        val leads = listOf(
            lead(1, LeadStatus.NEW, now),                 // due today
            lead(2, LeadStatus.CONTACTED, now - 2 * day), // overdue
            lead(3, LeadStatus.QUOTED, now + 3 * day, 20_000),
            lead(4, LeadStatus.WON, now - 5 * day, 15_000), // closed: not overdue
            lead(5, LeadStatus.WON, null, 5_000),
            lead(6, LeadStatus.LOST, now - day),
        )
        val state = DashboardViewModel.buildState(leads, "Shop", now)
        assertEquals(6, state.totalLeads)
        assertEquals(1, state.dueToday)
        assertEquals(1, state.overdue)
        assertEquals(2, state.wonCount)
        assertEquals(20_000L, state.wonValueKsh)
        assertEquals(20_000L, state.openValueKsh)
        assertEquals(listOf(2L, 1L), state.needsAttention.map { it.id }) // overdue first
        assertEquals(2, state.statusCounts[LeadStatus.WON])
    }
}
