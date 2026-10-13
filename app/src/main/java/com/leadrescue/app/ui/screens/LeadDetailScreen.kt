package com.leadrescue.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.ui.components.AppCard
import com.leadrescue.app.ui.components.Intents
import com.leadrescue.app.ui.components.LeadAvatar
import com.leadrescue.app.ui.components.StatusPill
import com.leadrescue.app.ui.components.WhatsAppComposerDialog
import com.leadrescue.app.ui.components.dueColor
import com.leadrescue.app.ui.components.reveal
import com.leadrescue.app.ui.theme.Coral
import com.leadrescue.app.ui.theme.WhatsAppGreen
import com.leadrescue.app.ui.theme.color
import com.leadrescue.app.ui.viewmodel.AppViewModelFactory
import com.leadrescue.app.ui.viewmodel.LeadDetailViewModel
import com.leadrescue.app.util.FollowUpDates
import com.leadrescue.app.util.KenyanPhone
import com.leadrescue.app.util.Ksh
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: LeadDetailViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var composing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    // The lead disappeared (deleted, or the id is unknown): leave the screen once.
    var left by remember { mutableStateOf(false) }
    LaunchedEffect(state.loading, state.lead) {
        if (!state.loading && state.lead == null && !left) {
            left = true
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    state.lead?.let { lead ->
                        IconButton(onClick = { onEdit(lead.id) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit lead") }
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete lead") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val lead = state.lead
        if (lead == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(lead, Modifier.reveal(0))

            Button(
                onClick = { composing = true },
                modifier = Modifier.fillMaxWidth().height(56.dp).reveal(1),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Follow up on WhatsApp", style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.reveal(2)) {
                FilledTonalButton(
                    onClick = {
                        if (!Intents.dial(context, lead.phone)) scope.launch { snackbar.showSnackbar("No phone app found") }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Call")
                }
                FilledTonalButton(
                    onClick = { onEdit(lead.id) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit details")
                }
            }

            StatusCard(lead, onStatus = { status ->
                viewModel.setStatus(status)
                val message = when (status) {
                    LeadStatus.WON -> "Sale won. Reminders for this lead are now off."
                    LeadStatus.LOST -> "Marked as lost. Reminders for this lead are now off."
                    else -> "Status updated to ${status.label}"
                }
                scope.launch { snackbar.showSnackbar(message) }
            }, modifier = Modifier.reveal(3))

            FollowUpCard(lead, state.now, onChange = viewModel::setFollowUp, modifier = Modifier.reveal(4))

            DetailsCard(lead, state.now, Modifier.reveal(5))

            Spacer(Modifier.height(32.dp))
        }
    }

    val lead = state.lead
    if (composing && lead != null) {
        WhatsAppComposerDialog(
            lead = lead,
            businessName = businessName,
            onDismiss = { composing = false },
            onOpened = {
                composing = false
                scope.launch {
                    val result = snackbar.showSnackbar(
                        message = "Sent the message?",
                        actionLabel = "Mark contacted",
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.markContacted()
                        snackbar.showSnackbar("Saved. Set the next follow-up date below.")
                    }
                }
            },
            onFailed = {
                composing = false
                scope.launch { snackbar.showSnackbar("Couldn't open WhatsApp. Is it installed?") }
            },
        )
    }

    if (confirmDelete && lead != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${lead.name}?") },
            text = { Text("This lead and its reminders will be removed from this phone. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("Delete", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Header(lead: Lead, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        LeadAvatar(lead.name, size = 80.dp)
        Spacer(Modifier.height(14.dp))
        Text(lead.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            KenyanPhone.format(lead.phone),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusPill(lead.status)
            Spacer(Modifier.width(8.dp))
            Text(
                "via ${lead.source.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusCard(lead: Lead, onStatus: (LeadStatus) -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            CardTitle("Status")
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeadStatus.entries.forEach { status ->
                    val c = status.color()
                    FilterChip(
                        selected = lead.status == status,
                        onClick = { if (lead.status != status) onStatus(status) },
                        label = { Text(status.label) },
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = c.copy(alpha = 0.16f),
                            selectedLabelColor = c,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowUpCard(lead: Lead, now: Long, onChange: (Long?) -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CardTitle("Next follow-up")
            if (!lead.status.isOpen) {
                Text(
                    "This lead is ${lead.status.label.lowercase()}, so there are no reminders. Change the status to plan another follow-up.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            lead.followUpAt?.let {
                Text(
                    FollowUpDates.relativeLabel(it, now),
                    style = MaterialTheme.typography.labelLarge,
                    color = dueColor(FollowUpDates.dueState(it, now)),
                )
            }
            val time = lead.followUpAt?.let { FollowUpDates.toLocalTime(it) } ?: FollowUpDates.DEFAULT_TIME
            val date = lead.followUpAt?.let { FollowUpDates.toDate(it) }
            FollowUpEditor(
                date = date,
                onDateChange = { d -> onChange(d?.let { FollowUpDates.toMillis(it, time) }) },
                timeLabel = FollowUpDates.formatTime(FollowUpDates.toMillis(LocalDate.now(), time)),
                currentTime = time,
                onPickTime = { t -> date?.let { onChange(FollowUpDates.toMillis(it, t)) } },
                summary = lead.followUpAt?.let { FollowUpDates.formatDayAndTime(it, now) },
                warnPast = lead.followUpAt?.let { it < now } ?: false,
            )
        }
    }
}

@Composable
private fun DetailsCard(lead: Lead, now: Long, modifier: Modifier = Modifier) {
    AppCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(PaddingValues(18.dp))) {
            CardTitle("Details")
            Spacer(Modifier.height(6.dp))
            DetailRow("Product / service", lead.product)
            DetailRow("Estimated value", lead.valueKsh?.let { Ksh.format(it) } ?: "Not set")
            DetailRow("Source", lead.source.label)
            DetailRow("Added", FollowUpDates.formatDayAndTime(lead.createdAt, now))
            DetailRow("Last contacted", lead.lastContactedAt?.let { FollowUpDates.formatDayAndTime(it, now) } ?: "Not yet")
            if (lead.notes.isNotBlank()) {
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text("Notes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(lead.notes, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.58f))
    }
}

@Composable
private fun CardTitle(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
}
