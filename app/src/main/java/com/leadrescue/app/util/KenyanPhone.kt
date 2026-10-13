package com.leadrescue.app.util

/**
 * Parses the many ways people write Kenyan mobile numbers and turns them into
 * one standard form: "+2547XXXXXXXX" or "+2541XXXXXXXX".
 *
 * Accepted examples: 0712 345 678, 712345678, +254712345678, 254712345678,
 * 00254712345678, +254 0712 345 678, 0110 123 456.
 * Numbers from other countries are accepted when they start with "+" (or "00").
 */
object KenyanPhone {

    private const val KENYA_CODE = "254"

    /** Returns the number in "+<country><number>" form, or null if it is not a valid number. */
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        // Only digits and common separators are allowed.
        if (trimmed.any { !it.isDigit() && it !in "+ -()." }) return null

        var digits = trimmed.filter { it.isDigit() }
        var international = trimmed.startsWith("+")
        if (!international && digits.startsWith("00")) {
            digits = digits.removePrefix("00")
            international = true
        }

        val kenyanLocal: String? = when {
            digits.startsWith(KENYA_CODE + "0") && digits.length == 13 -> digits.substring(4)
            digits.startsWith(KENYA_CODE) && digits.length == 12 -> digits.substring(3)
            !international && digits.startsWith("0") && digits.length == 10 -> digits.substring(1)
            !international && digits.length == 9 -> digits
            else -> null
        }

        if (kenyanLocal != null) {
            return if (isKenyanMobile(kenyanLocal)) "+$KENYA_CODE$kenyanLocal" else null
        }

        if (international && !digits.startsWith(KENYA_CODE) && digits.length in 8..15 && digits[0] != '0') {
            return "+$digits"
        }
        return null
    }

    fun isValid(input: String): Boolean = normalize(input) != null

    /** Kenyan mobile numbers are 9 digits after the country code and start with 7 or 1. */
    private fun isKenyanMobile(local: String): Boolean =
        local.length == 9 && (local[0] == '7' || local[0] == '1')

    /** "+254712345678" -> "+254 712 345 678". Other numbers are returned unchanged. */
    fun format(normalized: String): String {
        if (normalized.startsWith("+$KENYA_CODE") && normalized.length == 13) {
            val local = normalized.substring(4)
            return "+$KENYA_CODE ${local.substring(0, 3)} ${local.substring(3, 6)} ${local.substring(6)}"
        }
        return normalized
    }

    /** Digits only, as WhatsApp's wa.me links expect: "+254712345678" -> "254712345678". */
    fun forWhatsApp(normalized: String): String = normalized.filter { it.isDigit() }
}
