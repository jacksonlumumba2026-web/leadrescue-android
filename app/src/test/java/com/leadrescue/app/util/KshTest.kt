package com.leadrescue.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KshTest {

    @Test
    fun `formats with thousands separators`() {
        assertEquals("KSh 0", Ksh.format(0))
        assertEquals("KSh 950", Ksh.format(950))
        assertEquals("KSh 12,500", Ksh.format(12_500))
        assertEquals("KSh 1,250,000", Ksh.format(1_250_000))
    }

    @Test
    fun `compact format for the dashboard`() {
        assertEquals("KSh 9,999", Ksh.formatCompact(9_999))
        assertEquals("KSh 12.5K", Ksh.formatCompact(12_500))
        assertEquals("KSh 120K", Ksh.formatCompact(120_000))
        assertEquals("KSh 2.3M", Ksh.formatCompact(2_300_000))
        assertEquals("KSh 1B", Ksh.formatCompact(1_000_000_000))
    }

    @Test
    fun `parses typed amounts`() {
        assertEquals(12_500L, Ksh.parse("12500"))
        assertEquals(12_500L, Ksh.parse("12,500"))
        assertEquals(12_500L, Ksh.parse("KSh 12,500"))
        assertEquals(12_500L, Ksh.parse("12500.00"))
        assertNull(Ksh.parse(""))
        assertNull(Ksh.parse("abc"))
        assertNull(Ksh.parse("1234567890123"))
    }
}
