package com.leadrescue.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KenyanPhoneTest {

    @Test
    fun `normalises common Kenyan formats`() {
        val expected = "+254712345678"
        listOf(
            "0712345678",
            "0712 345 678",
            "0712-345-678",
            "712345678",
            "+254712345678",
            "+254 712 345 678",
            "254712345678",
            "00254712345678",
            "+254 0712 345 678",
            "(0712) 345678",
        ).forEach { assertEquals("input: $it", expected, KenyanPhone.normalize(it)) }
    }

    @Test
    fun `accepts newer 01xx numbers`() {
        assertEquals("+254110123456", KenyanPhone.normalize("0110 123 456"))
        assertEquals("+254110123456", KenyanPhone.normalize("+254110123456"))
    }

    @Test
    fun `rejects invalid numbers`() {
        listOf(
            "",
            "   ",
            "12345",
            "0712345",          // too short
            "07123456789",      // too long
            "0201234567",       // Nairobi landline, not on WhatsApp mobile
            "+2547123",         // Kenyan code but too short
            "0712abc678",       // letters
            "+254812345678",    // Kenyan number not starting with 7 or 1
        ).forEach { assertNull("input: '$it'", KenyanPhone.normalize(it)) }
    }

    @Test
    fun `accepts international numbers with a plus`() {
        assertEquals("+447911123456", KenyanPhone.normalize("+44 7911 123456"))
        assertEquals("+256772123456", KenyanPhone.normalize("+256 772 123 456"))
    }

    @Test
    fun `formats for display and WhatsApp`() {
        assertEquals("+254 712 345 678", KenyanPhone.format("+254712345678"))
        assertEquals("+447911123456", KenyanPhone.format("+447911123456"))
        assertEquals("254712345678", KenyanPhone.forWhatsApp("+254712345678"))
    }
}
