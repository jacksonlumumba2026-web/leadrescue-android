package com.leadrescue.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.leadrescue.app.ui.LeadRescueRoot
import com.leadrescue.app.ui.theme.LeadRescueTheme

class MainActivity : ComponentActivity() {

    /** Lead to open because a reminder notification was tapped. */
    private val pendingLeadId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) readLeadId(intent)
        setContent {
            LeadRescueTheme {
                LeadRescueRoot(
                    openLeadId = pendingLeadId.value,
                    onLeadOpened = { pendingLeadId.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readLeadId(intent)
    }

    private fun readLeadId(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_LEAD_ID, -1L) ?: -1L
        if (id > 0) pendingLeadId.value = id
    }

    companion object {
        const val EXTRA_LEAD_ID = "lead_id"
    }
}
