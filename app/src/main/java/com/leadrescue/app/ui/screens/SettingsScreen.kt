package com.leadrescue.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.leadrescue.app.BuildConfigInfo
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.ui.components.AppCard
import com.leadrescue.app.ui.components.Intents
import com.leadrescue.app.ui.components.rememberNotificationAccess
import com.leadrescue.app.ui.theme.Gold500
import com.leadrescue.app.ui.theme.StatusWon
import com.leadrescue.app.util.WhatsAppMessages

@Composable
fun SettingsScreen(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val notifications = rememberNotificationAccess()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        SettingsCard(Icons.Filled.Home, "Your business") {
            OutlinedTextField(
                value = businessName,
                onValueChange = { onBusinessNameChange(it.take(60)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Business name") },
                placeholder = { Text("e.g. Mama Njeri Fashions") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            )
            Text("Message preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                WhatsAppMessages.build("Wanjiku", "a 2-seater sofa", LeadStatus.CONTACTED, null, businessName),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
            )
        }

        SettingsCard(Icons.Filled.Notifications, "Reminders") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (notifications.enabled) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (notifications.enabled) StatusWon else Gold500,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (notifications.enabled) "Notifications are on" else "Notifications are off",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Body("You get a reminder at each lead's follow-up time, plus a summary at about 8:00 AM when follow-ups are due or overdue.")
            FilledTonalButton(onClick = notifications.request) {
                Text(if (notifications.enabled) "Notification settings" else "Turn on notifications")
            }
        }

        SettingsCard(Icons.Filled.Settings, "Samsung battery tip") {
            Body(
                "Samsung phones can delay reminders to save battery. For on-time reminders, open App info, " +
                    "tap Battery and choose Unrestricted. Reminders can still arrive a few minutes late when the phone is idle."
            )
            FilledTonalButton(onClick = { Intents.openAppSettings(context) }) { Text("Open App info") }
        }

        SettingsCard(Icons.Filled.Lock, "Privacy") {
            Body(
                "Your leads are saved only on this phone. LeadRescue doesn't read your WhatsApp chats and never sends " +
                    "messages for you. It opens WhatsApp with a message ready, and you choose to send it."
            )
            Body("Uninstalling the app deletes your saved leads.")
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "LeadRescue ${BuildConfigInfo.versionName(context)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun Body(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
}
