package com.leadrescue.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.leadrescue.app.data.Lead
import com.leadrescue.app.ui.theme.WhatsAppGreen
import com.leadrescue.app.util.KenyanPhone
import com.leadrescue.app.util.WhatsAppMessages

/**
 * Lets the owner review and edit the follow-up message, then opens WhatsApp with it.
 * The message is NOT sent automatically: the owner taps send inside WhatsApp.
 *
 * [onOpened] is called after WhatsApp opened; [onFailed] if no app could open the link.
 */
@Composable
fun WhatsAppComposerDialog(
    lead: Lead,
    businessName: String,
    onDismiss: () -> Unit,
    onOpened: () -> Unit,
    onFailed: () -> Unit,
) {
    val context = LocalContext.current
    var message by rememberSaveable(lead.id) {
        mutableStateOf(WhatsAppMessages.build(lead.name, lead.product, lead.status, lead.valueKsh, businessName))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Follow up with ${lead.name.substringBefore(" ")}") },
        text = {
            Column {
                Text(
                    "To ${KenyanPhone.format(lead.phone)}. Edit the message if you like. WhatsApp opens with it ready, and you tap send.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 9,
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val opened = Intents.openUrl(context, WhatsAppMessages.link(lead.phone, message.trim()))
                    if (opened) onOpened() else onFailed()
                },
                enabled = message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open WhatsApp")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
