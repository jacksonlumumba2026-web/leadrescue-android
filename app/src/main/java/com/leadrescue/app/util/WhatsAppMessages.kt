package com.leadrescue.app.util

import com.leadrescue.app.data.LeadStatus
import java.net.URLEncoder

/**
 * Builds the follow-up message and the WhatsApp link.
 * The app only opens WhatsApp with the text filled in. The business owner reviews it and taps send.
 */
object WhatsAppMessages {

    fun build(
        customerName: String,
        product: String,
        status: LeadStatus,
        valueKsh: Long?,
        businessName: String,
    ): String {
        val first = customerName.trim().substringBefore(" ").ifBlank { "there" }
        val item = product.trim().ifBlank { "your request" }
        val from = businessName.trim()
        val intro = if (from.isNotEmpty()) "Hello $first, this is $from." else "Hello $first,"

        val body = when (status) {
            LeadStatus.NEW ->
                "Thank you for your interest in $item. I'd be happy to share prices and details. When is a good time to talk?"
            LeadStatus.CONTACTED ->
                "I'm following up on $item that you asked about. Do you have any questions, or would you like me to send you a quote?"
            LeadStatus.QUOTED -> {
                val price = valueKsh?.let { " of ${Ksh.format(it)}" } ?: ""
                "I'm following up on the quote$price for $item. Would you like to go ahead? I'm happy to answer any questions."
            }
            LeadStatus.WON ->
                "Thank you for choosing us for $item! How is everything going? Let me know if you need anything else."
            LeadStatus.LOST ->
                "I hope you're well. Just checking in. If you still need $item, we would be glad to help."
        }
        return "$intro $body"
    }

    /** https://wa.me/254712345678?text=... opens WhatsApp (or WhatsApp Business) at that chat. */
    fun link(normalizedPhone: String, message: String): String {
        val text = URLEncoder.encode(message, "UTF-8").replace("+", "%20")
        return "https://wa.me/${KenyanPhone.forWhatsApp(normalizedPhone)}?text=$text"
    }
}
