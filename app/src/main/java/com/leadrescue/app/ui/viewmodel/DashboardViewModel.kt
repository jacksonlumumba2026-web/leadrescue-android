package com.leadrescue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadRepository
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.data.SettingsStore
import com.leadrescue.app.util.DueState
import com.leadrescue.app.util.FollowUpDates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val businessName: String = "",
    val now: Long = System.currentTimeMillis(),
    val totalLeads: Int = 0,
    val dueToday: Int = 0,
    val overdue: Int = 0,
    val wonCount: Int = 0,
    val wonValueKsh: Long = 0,
    val openValueKsh: Long = 0,
    val statusCounts: Map<LeadStatus, Int> = emptyMap(),
    /** Open leads that are overdue or due today, most urgent first. */
    val needsAttention: List<Lead> = emptyList(),
)

class DashboardViewModel(
    private val repository: LeadRepository,
    settings: SettingsStore,
    ticks: Flow<Long>,
) : ViewModel() {

    val businessName: StateFlow<String> = settings.businessName

    val state: StateFlow<DashboardUiState> =
        combine(repository.observeAll(), settings.businessName, ticks) { leads, business, now ->
            buildState(leads, business, now)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun markContacted(lead: Lead) {
        viewModelScope.launch { repository.markContacted(lead) }
    }

    companion object {
        fun buildState(leads: List<Lead>, business: String, now: Long): DashboardUiState {
            val open = leads.filter { it.status.isOpen }
            val overdue = open.filter { FollowUpDates.dueState(it.followUpAt, now) == DueState.OVERDUE }
            val today = open.filter { FollowUpDates.dueState(it.followUpAt, now) == DueState.TODAY }
            val won = leads.filter { it.status == LeadStatus.WON }
            return DashboardUiState(
                loading = false,
                businessName = business,
                now = now,
                totalLeads = leads.size,
                dueToday = today.size,
                overdue = overdue.size,
                wonCount = won.size,
                wonValueKsh = won.sumOf { it.valueKsh ?: 0L },
                openValueKsh = open.sumOf { it.valueKsh ?: 0L },
                statusCounts = LeadStatus.entries.associateWith { s -> leads.count { it.status == s } },
                needsAttention = (overdue.sortedBy { it.followUpAt } + today.sortedBy { it.followUpAt }),
            )
        }
    }
}
