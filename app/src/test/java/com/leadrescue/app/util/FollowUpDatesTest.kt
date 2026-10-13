package com.leadrescue.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class FollowUpDatesTest {

    private val nairobi = ZoneId.of("Africa/Nairobi")
    private val today = LocalDate.of(2026, 10, 13)
    private val now = FollowUpDates.toMillis(today, LocalTime.of(14, 0), nairobi)

    private fun at(date: LocalDate, hour: Int = 9) = FollowUpDates.toMillis(date, LocalTime.of(hour, 0), nairobi)

    @Test
    fun `due state uses calendar days`() {
        assertEquals(DueState.NONE, FollowUpDates.dueState(null, now, nairobi))
        assertEquals(DueState.OVERDUE, FollowUpDates.dueState(at(today.minusDays(1), 23), now, nairobi))
        // Earlier today is still "due today", not overdue.
        assertEquals(DueState.TODAY, FollowUpDates.dueState(at(today, 9), now, nairobi))
        assertEquals(DueState.TODAY, FollowUpDates.dueState(at(today, 18), now, nairobi))
        assertEquals(DueState.UPCOMING, FollowUpDates.dueState(at(today.plusDays(1), 0), now, nairobi))
    }

    @Test
    fun `relative labels`() {
        assertEquals("No follow-up set", FollowUpDates.relativeLabel(null, now, nairobi))
        assertEquals("Overdue by 3 days", FollowUpDates.relativeLabel(at(today.minusDays(3)), now, nairobi))
        assertEquals("Overdue since yesterday", FollowUpDates.relativeLabel(at(today.minusDays(1)), now, nairobi))
        assertEquals("Due today · 9:00 AM", FollowUpDates.relativeLabel(at(today), now, nairobi))
        assertEquals("Tomorrow · 3:00 PM", FollowUpDates.relativeLabel(at(today.plusDays(1), 15), now, nairobi))
        assertEquals("In 4 days", FollowUpDates.relativeLabel(at(today.plusDays(4)), now, nairobi))
        assertEquals("Fri, 23 Oct", FollowUpDates.relativeLabel(at(today.plusDays(10)), now, nairobi))
    }

    @Test
    fun `day formatting`() {
        assertEquals("Today", FollowUpDates.formatDay(at(today), now, nairobi))
        assertEquals("Yesterday", FollowUpDates.formatDay(at(today.minusDays(1)), now, nairobi))
        assertEquals("Thu, 14 Jan 2027", FollowUpDates.formatDay(at(LocalDate.of(2027, 1, 14)), now, nairobi))
        assertEquals("Today · 9:00 AM", FollowUpDates.formatDayAndTime(at(today), now, nairobi))
    }
}
