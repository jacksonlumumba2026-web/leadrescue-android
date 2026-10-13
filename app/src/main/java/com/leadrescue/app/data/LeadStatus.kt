package com.leadrescue.app.data

/** Where a lead is in the sales journey. Stored in Room by [name], so never rename the constants. */
enum class LeadStatus(val label: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    QUOTED("Quoted"),
    WON("Won"),
    LOST("Lost");

    /** Open leads still need follow-up. Won and Lost leads are finished. */
    val isOpen: Boolean get() = this != WON && this != LOST
}

/** How the customer first reached the business. Stored in Room by [name]. */
enum class LeadSource(val label: String) {
    WHATSAPP("WhatsApp"),
    PHONE_CALL("Phone call"),
    WALK_IN("Walk-in"),
    FACEBOOK("Facebook"),
    INSTAGRAM("Instagram"),
    TIKTOK("TikTok"),
    REFERRAL("Referral"),
    WEBSITE("Website"),
    OTHER("Other"),
}
