package com.leadrescue.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadFilter
import com.leadrescue.app.ui.components.EmptyState
import com.leadrescue.app.ui.components.LeadCard
import com.leadrescue.app.ui.components.WhatsAppComposerDialog
import com.leadrescue.app.ui.viewmodel.AppViewModelFactory
import com.leadrescue.app.ui.viewmodel.LeadsViewModel
import kotlinx.coroutines.launch

@Composable
fun LeadsScreen(
    businessName: String,
    onAddLead: () -> Unit,
    onOpenLead: (Long) -> Unit,
    viewModel: LeadsViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
                Text("Leads", style = MaterialTheme.typography.headlineLarge)
                Text(
                    when (state.totalLeads) {
                        0 -> "No leads yet"
                        1 -> "1 lead saved on this phone"
                        else -> "${state.totalLeads} leads saved on this phone"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search name, product or phone") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(LeadFilter.entries.toList(), key = { it.name }) { f ->
                    val count = state.counts[f] ?: 0
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(if (count > 0) "${f.label}  $count" else f.label) },
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            when {
                state.loading -> Unit
                state.totalLeads == 0 -> EmptyState(
                    icon = Icons.Filled.Add,
                    title = "No leads yet",
                    message = "Save every customer who asks about your products. You'll see them here with their next follow-up.",
                    action = { Button(onClick = onAddLead) { Text("Add your first lead") } },
                )
                state.leads.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = "Nothing matches",
                    message = "Try another search or filter.",
                    action = {
                        TextButton(onClick = { viewModel.setQuery(""); viewModel.setFilter(LeadFilter.ALL) }) {
                            Text("Show all leads")
                        }
                    },
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.leads, key = { it.id }) { lead ->
                        LeadCard(
                            lead = lead,
                            now = state.now,
                            onClick = { onOpenLead(lead.id) },
                            onWhatsApp = { composing = lead },
                            modifier = Modifier.animateItem(),
                        )
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
                    if (result == SnackbarResult.ActionPerformed) viewModel.markContacted(lead)
                }
            },
            onFailed = {
                composing = null
                scope.launch { snackbar.showSnackbar("Couldn't open WhatsApp. Is it installed?") }
            },
        )
    }
}
