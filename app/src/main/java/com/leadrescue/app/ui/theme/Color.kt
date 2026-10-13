package com.leadrescue.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.leadrescue.app.data.LeadStatus

// Brand palette: deep emerald (growth, money) with a warm gold accent.
val Emerald900 = Color(0xFF06372D)
val Emerald800 = Color(0xFF0A4F41)
val Emerald700 = Color(0xFF0E7C66)
val Emerald500 = Color(0xFF16A889)
val Emerald200 = Color(0xFFA6E9D7)
val Emerald50 = Color(0xFFE8F7F2)
val Gold500 = Color(0xFFF2B441)
val Gold100 = Color(0xFFFFF1D1)
val Ink = Color(0xFF0A1F1A)
val Slate600 = Color(0xFF55615D)
val Slate400 = Color(0xFF8C9894)
val Mist = Color(0xFFF5F7F4)
val Coral = Color(0xFFE5484D)
val WhatsAppGreen = Color(0xFF1DAA61)

// Status colours used for pills, chips and the pipeline bar.
val StatusNew = Color(0xFF3B82F6)
val StatusContacted = Color(0xFF8B5CF6)
val StatusQuoted = Color(0xFFF59E0B)
val StatusWon = Color(0xFF10B981)
val StatusLost = Color(0xFF94A3B8)

fun LeadStatus.color(): Color = when (this) {
    LeadStatus.NEW -> StatusNew
    LeadStatus.CONTACTED -> StatusContacted
    LeadStatus.QUOTED -> StatusQuoted
    LeadStatus.WON -> StatusWon
    LeadStatus.LOST -> StatusLost
}
