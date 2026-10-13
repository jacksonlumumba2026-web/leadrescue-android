package com.leadrescue.app.util

import com.leadrescue.app.data.LeadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppMessagesTest {

    @Test
    fun `uses first name, product and business`() {
        val msg = WhatsAppMessages.build("Wanjiku Kamau", "a sofa", LeadStatus.NEW, null, "Mama Njeri Furniture")
        assertTrue(msg.startsWith("Hello Wanjiku, this is Mama Njeri Furniture."))
        assertTrue(msg.contains("a sofa"))
    }

    @Test
    fun `quote message includes KSh value`() {
        val msg = WhatsAppMessages.build("Otieno", "solar panels", LeadStatus.QUOTED, 45_000, "")
        assertTrue(msg.startsWith("Hello Otieno,"))
        assertTrue(msg.contains("KSh 45,000"))
    }

    @Test
    fun `every status has a message`() {
        LeadStatus.entries.forEach {
            assertFalse(WhatsAppMessages.build("A", "B", it, null, "").isBlank())
        }
    }

    @Test
    fun `link is a wa_me url with encoded text`() {
        val link = WhatsAppMessages.link("+254712345678", "Hi Jane & co, 100% ready?")
        assertEquals("https://wa.me/254712345678?text=Hi%20Jane%20%26%20co%2C%20100%25%20ready%3F", link)
    }
}
