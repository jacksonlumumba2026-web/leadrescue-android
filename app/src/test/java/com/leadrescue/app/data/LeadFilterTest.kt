package com.leadrescue.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeadFilterTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = System.currentTimeMillis()

    private fun lead(
        status: LeadStatus = LeadStatus.NEW,
        followUpAt: Long? = null,
        name: String = "Jane Wambui",
        product: String = "School uniform",
        phone: String = "+254712345678",
        notes: String = "",
    ) = Lead(
        name = name, phone = phone, product = product, source = LeadSource.WHATSAPP,
        notes = notes, status = status, followUpAt = followUpAt, createdAt = 0, updatedAt = 0,
    )

    @Test
    fun `overdue and due today only count open leads`() {
        assertTrue(LeadFilter.OVERDUE.matches(lead(followUpAt = now - 3 * day), now))
        assertFalse(LeadFilter.OVERDUE.matches(lead(status = LeadStatus.WON, followUpAt = now - 3 * day), now))
        assertTrue(LeadFilter.DUE_TODAY.matches(lead(followUpAt = now), now))
        assertFalse(LeadFilter.DUE_TODAY.matches(lead(status = LeadStatus.LOST, followUpAt = now), now))
        assertFalse(LeadFilter.DUE_TODAY.matches(lead(followUpAt = null), now))
    }

    @Test
    fun `status filters`() {
        assertTrue(LeadFilter.QUOTED.matches(lead(status = LeadStatus.QUOTED), now))
        assertFalse(LeadFilter.QUOTED.matches(lead(status = LeadStatus.NEW), now))
        assertTrue(LeadFilter.ALL.matches(lead(status = LeadStatus.LOST), now))
        assertEquals(LeadFilter.ALL, LeadFilter.fromName("nonsense"))
        assertEquals(LeadFilter.OVERDUE, LeadFilter.fromName("OVERDUE"))
    }

    @Test
    fun `search by name, product, notes and phone`() {
        val l = lead(notes = "Wants delivery to Thika")
        assertTrue(l.matchesQuery(""))
        assertTrue(l.matchesQuery("jane"))
        assertTrue(l.matchesQuery("uniform"))
        assertTrue(l.matchesQuery("thika"))
        assertTrue(l.matchesQuery("0712 345"))
        assertTrue(l.matchesQuery("712345678"))
        assertTrue(l.matchesQuery("+254712"))
        assertFalse(l.matchesQuery("otieno"))
        assertFalse(l.matchesQuery("07"))
    }
}
