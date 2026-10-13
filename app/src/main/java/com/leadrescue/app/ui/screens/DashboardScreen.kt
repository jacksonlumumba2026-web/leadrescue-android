package com.leadrescue.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadFilter
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.ui.components.AppCard
import com.leadrescue.app.ui.components.EmptyState
import com.leadrescue.app.ui.components.LeadCard
import com.leadrescue.app.ui.components.SectionHeader
import com.leadrescue.app.ui.components.WhatsAppComposerDialog
import com.leadrescue.app.ui.components.rememberNotificationAccess
import com.leadrescue.app.ui.components.reveal
import com.leadrescue.app.ui.theme.Coral
import com.leadrescue.app.ui.theme.Emerald700
import com.leadrescue.app.ui.theme.Emerald800
import com.leadrescue.app.ui.theme.Emerald900
import com.leadrescue.app.ui.theme.Gold500
import com.leadrescue.app.ui.theme.Ink
import com.leadrescue.app.ui.theme.StatusNew
import com.leadrescue.app.ui.theme.StatusWon
import com.leadrescue.app.ui.theme.color
import com.leadrescue.app.ui.viewmodel.AppViewModelFactory
import com.leadrescue.app.ui.viewmodel.DashboardUiState
import com.leadrescue.app.ui.viewmodel.DashboardViewModel
import com.leadrescue.app.util.Ksh
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun DashboardScreen(
    onAddLead: () -> Unit,
    onOpenLead: (Long) -> Unit,
    onOpenLeads: (LeadFilter) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()
    val notifications = rememberNotificationAccess()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var composing by remember { mutableStateOf<Lead?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddLead,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add lead") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 112.dp),
        ) {
            item(key = "hero") {
                Column {
                    Hero(state)
                    StatsGrid(state, onOpenLeads, Modifier.pullUp(44.dp).padding(horizontal = 20.dp))
                }
            }

            if (!notifications.enabled) {
                item(key = "notif") {
                    RemindersOffCard(onTurnOn = notifications.request, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp).reveal(4))
                }
            }

            if (!state.loading && state.totalLeads == 0) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.Person,
                        title = "Add your first lead",
                        message = "Every inquiry from WhatsApp, calls or walk-ins is a potential sale. Save it here and LeadRescue reminds you to follow up.",
                        modifier = Modifier.reveal(5),
                        action = {
                            Button(onClick = onAddLead, contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add a lead")
                            }
                        },
                    )
                }
            } else if (state.totalLeads > 0) {
                item(key = "attention-header") {
                    SectionHeader(
                        title = "Follow up now",
                        actionLabel = "All leads",
                        onAction = { onOpenLeads(LeadFilter.ALL) },
                        modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 28.dp, bottom = 8.dp).reveal(5),
                    )
                }
                if (state.needsAttention.isEmpty()) {
                    item(key = "caught-up") {
                        CaughtUpCard(Modifier.padding(horizontal = 20.dp).reveal(6))
                    }
                } else {
                    items(state.needsAttention.take(8), key = { it.id }) { lead ->
                        LeadCard(
                            lead = lead,
                            now = state.now,
                            onClick = { onOpenLead(lead.id) },
                            onWhatsApp = { composing = lead },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp).reveal(6),
                        )
                    }
                    if (state.needsAttention.size > 8) {
                        item(key = "more") {
                            TextButton(
                                onClick = { onOpenLeads(if (state.overdue > 0) LeadFilter.OVERDUE else LeadFilter.DUE_TODAY) },
                                modifier = Modifier.padding(horizontal = 12.dp),
                            ) { Text("See ${state.needsAttention.size - 8} more") }
                        }
                    }
                }

                item(key = "pipeline") {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp).reveal(7)) {
                        Text("Pipeline", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        PipelineCard(state.statusCounts, state.totalLeads)
                    }
                }
            }
        }
    }

    composing?.let { lead ->
        WhatsAppComposerDialog(
            lead = lead,
            businessName = businessName,
            onDismiss = { composing = null },
            onOpened = {
                composing = null
                scope.launch {
                    val result = snackbar.showSnackbar(
                        message = "Sent the message to ${lead.name.substringBefore(" ")}?",
                        actionLabel = "Mark contacted",
                        withDismissAction = true,
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) viewModel.markContacted(lead)
                }
            },
            onFailed = {
                composing = null
                scope.launch { snackbar.showSnackbar("Couldn't open WhatsApp. Is it installed?") }
            },
        )
    }
}

/** Shifts content up by [amount] so it overlaps the element above (used for the stat cards over the hero). */
private fun Modifier.pullUp(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val px = amount.roundToPx()
    layout(placeable.width, (placeable.height - px).coerceAtLeast(0)) { placeable.place(0, -px) }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun Hero(state: DashboardUiState) {
    val waiting = state.dueToday + state.overdue
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.linearGradient(listOf(Emerald900, Emerald800, Emerald700)))
    ) {
        // Soft decorative glow in the corner.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp)
                .size(220.dp)
                .background(
                    Brush.radialGradient(listOf(Gold500.copy(alpha = 0.22f), Color.Transparent)),
                    CircleShape,
                )
        )
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 72.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.reveal(0)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Gold500))
                Spacer(Modifier.width(8.dp))
                Text(
                    "LEADRESCUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                greeting() + if (state.businessName.isNotBlank()) ", ${state.businessName}" else "",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.reveal(1),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    state.loading -> " "
                    waiting == 1 -> "1 customer is waiting for your follow-up"
                    waiting > 1 -> "$waiting customers are waiting for your follow-up"
                    state.totalLeads == 0 -> "Never lose a customer to a forgotten follow-up"
                    else -> "You're all caught up"
                },
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.reveal(2),
            )
            Spacer(Modifier.height(20.dp))
            // Frosted-glass money summary.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .reveal(3),
            ) {
                MoneyFigure("Sales won", Ksh.formatCompact(state.wonValueKsh), Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.18f)).align(Alignment.CenterVertically))
                MoneyFigure("Open pipeline", Ksh.formatCompact(state.openValueKsh), Modifier.weight(1f).padding(start = 18.dp))
            }
        }
    }
}

@Composable
private fun MoneyFigure(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatsGrid(state: DashboardUiState, onOpenLeads: (LeadFilter) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Total leads",
                value = state.totalLeads.toString(),
                icon = Icons.Filled.Person,
                accent = StatusNew,
                onClick = { onOpenLeads(LeadFilter.ALL) },
                modifier = Modifier.weight(1f).reveal(2),
            )
            StatCard(
                label = "Due today",
                value = state.dueToday.toString(),
                icon = Icons.Filled.DateRange,
                accent = Gold500,
                onClick = { onOpenLeads(LeadFilter.DUE_TODAY) },
                modifier = Modifier.weight(1f).reveal(3),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Overdue",
                value = state.overdue.toString(),
                icon = Icons.Filled.Warning,
                accent = Coral,
                onClick = { onOpenLeads(LeadFilter.OVERDUE) },
                modifier = Modifier.weight(1f).reveal(4),
            )
            StatCard(
                label = "Sales won",
                value = state.wonCount.toString(),
                caption = if (state.wonValueKsh > 0) Ksh.format(state.wonValueKsh) else null,
                icon = Icons.Filled.Star,
                accent = StatusWon,
                onClick = { onOpenLeads(LeadFilter.WON) },
                modifier = Modifier.weight(1f).reveal(5),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (caption != null) {
                Text(caption, style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RemindersOffCard(onTurnOn: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Reminders are off", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                "Turn on notifications so you never miss a follow-up.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Button(
            onClick = onTurnOn,
            colors = ButtonDefaults.buttonColors(containerColor = Gold500, contentColor = Ink),
        ) { Text("Turn on") }
    }
}

@Composable
private fun CaughtUpCard(modifier: Modifier = Modifier) {
    AppCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusWon, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text("No follow-ups due", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Great work. Upcoming follow-ups will appear here on their day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A single stacked bar showing how leads are spread across statuses, with a legend. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PipelineCard(counts: Map<LeadStatus, Int>, total: Int) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                LeadStatus.entries.forEach { status ->
                    val count = counts[status] ?: 0
                    if (count > 0) {
                        Box(Modifier.weight(count.toFloat()).height(12.dp).background(status.color()))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LeadStatus.entries.forEach { status ->
                    val count = counts[status] ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(status.color()))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${status.label} $count",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (total > 0) {
                Spacer(Modifier.height(12.dp))
                val won = counts[LeadStatus.WON] ?: 0
                val closed = won + (counts[LeadStatus.LOST] ?: 0)
                Text(
                    if (closed > 0) "Win rate: ${won * 100 / closed}% of closed leads" else "Close your first sale to see your win rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
