package com.leadrescue.app.util

import java.text.NumberFormat
import java.util.Locale

/** Kenyan Shilling helpers. Amounts are whole shillings stored as Long. */
object Ksh {

    private const val MAX_DIGITS = 12 // up to KSh 999,999,999,999

    /** 12500 -> "KSh 12,500" */
    fun format(amount: Long): String =
        "KSh " + NumberFormat.getIntegerInstance(Locale.US).format(amount)

    /** Short form for tight spaces: 950 -> "KSh 950", 12500 -> "KSh 12.5K", 2300000 -> "KSh 2.3M" */
    fun formatCompact(amount: Long): String = when {
        amount >= 1_000_000_000 -> "KSh " + oneDecimal(amount / 1_000_000_000.0) + "B"
        amount >= 1_000_000 -> "KSh " + oneDecimal(amount / 1_000_000.0) + "M"
        amount >= 10_000 -> "KSh " + oneDecimal(amount / 1_000.0) + "K"
        else -> format(amount)
    }

    private fun oneDecimal(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }

    /**
     * Reads what the user typed ("12,500", "KSh 12500", "12500.00") into whole shillings.
     * Returns null for blank input or input that is not a number.
     */
    fun parse(input: String): Long? {
        val cleaned = input.trim()
            .removePrefix("KSh").removePrefix("KES").removePrefix("Ksh").removePrefix("ksh")
            .replace(",", "").replace(" ", "")
            .substringBefore(".")
        if (cleaned.isEmpty() || cleaned.length > MAX_DIGITS || !cleaned.all { it.isDigit() }) return null
        return cleaned.toLong()
    }
}
