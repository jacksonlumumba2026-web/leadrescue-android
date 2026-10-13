package com.leadrescue.app.data

import com.leadrescue.app.util.DueState
import com.leadrescue.app.util.FollowUpDates

/** The filter chips on the Leads screen. */
enum class LeadFilter(val label: String) {
    ALL("All"),
    OVERDUE("Overdue"),
    DUE_TODAY("Due today"),
    NEW("New"),
    CONTACTED("Contacted"),
    QUOTED("Quoted"),
    WON("Won"),
    LOST("Lost");

    fun matches(lead: Lead, now: Long): Boolean = when (this) {
        ALL -> true
        OVERDUE -> lead.status.isOpen && FollowUpDates.dueState(lead.followUpAt, now) == DueState.OVERDUE
        DUE_TODAY -> lead.status.isOpen && FollowUpDates.dueState(lead.followUpAt, now) == DueState.TODAY
        NEW -> lead.status == LeadStatus.NEW
        CONTACTED -> lead.status == LeadStatus.CONTACTED
        QUOTED -> lead.status == LeadStatus.QUOTED
        WON -> lead.status == LeadStatus.WON
        LOST -> lead.status == LeadStatus.LOST
    }

    companion object {
        fun fromName(name: String?): LeadFilter = entries.firstOrNull { it.name == name } ?: ALL
    }
}

/**
 * Search across name, product, notes and phone number.
 * Phone search ignores spaces and works with "0712...", "712..." or "254712...".
 */
fun Lead.matchesQuery(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    if (name.contains(q, ignoreCase = true) ||
        product.contains(q, ignoreCase = true) ||
        notes.contains(q, ignoreCase = true)
    ) return true
    val digits = q.filter { it.isDigit() }.trimStart('0')
    return digits.length >= 3 && phone.filter { it.isDigit() }.contains(digits)
}
