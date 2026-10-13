package com.leadrescue.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Whether a follow-up is late, due today, coming up, or not set. */
enum class DueState { NONE, OVERDUE, TODAY, UPCOMING }

object FollowUpDates {

    val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)

    private val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
    private val dateYearFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
    private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    /** Compares calendar days, so a follow-up at 9:00 today is "due today" all day, and "overdue" from tomorrow. */
    fun dueState(followUpAt: Long?, now: Long, zone: ZoneId = ZoneId.systemDefault()): DueState {
        if (followUpAt == null) return DueState.NONE
        val day = toDate(followUpAt, zone)
        val today = toDate(now, zone)
        return when {
            day.isBefore(today) -> DueState.OVERDUE
            day.isEqual(today) -> DueState.TODAY
            else -> DueState.UPCOMING
        }
    }

    fun toDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun toMillis(date: LocalDate, time: LocalTime, zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDateTime.of(date, time).atZone(zone).toInstant().toEpochMilli()

    fun toLocalTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalTime =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalTime().withSecond(0).withNano(0)

    /** "Today", "Tomorrow", "Yesterday" or "Thu, 16 Oct" (year added when it is not this year). */
    fun formatDay(millis: Long, now: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val day = toDate(millis, zone)
        val today = toDate(now, zone)
        return when (ChronoUnit.DAYS.between(today, day)) {
            0L -> "Today"
            1L -> "Tomorrow"
            -1L -> "Yesterday"
            else -> if (day.year == today.year) day.format(dateFormat) else day.format(dateYearFormat)
        }
    }

    fun formatTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalTime().format(timeFormat)

    fun formatDayAndTime(millis: Long, now: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        formatDay(millis, now, zone) + " · " + formatTime(millis, zone)

    /** Short label shown on lead cards, e.g. "Overdue by 2 days", "Due today", "In 3 days". */
    fun relativeLabel(followUpAt: Long?, now: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        if (followUpAt == null) return "No follow-up set"
        val days = ChronoUnit.DAYS.between(toDate(now, zone), toDate(followUpAt, zone))
        return when {
            days < -1 -> "Overdue by ${-days} days"
            days == -1L -> "Overdue since yesterday"
            days == 0L -> "Due today · ${formatTime(followUpAt, zone)}"
            days == 1L -> "Tomorrow · ${formatTime(followUpAt, zone)}"
            days < 7 -> "In $days days"
            else -> formatDay(followUpAt, now, zone)
        }
    }
}
