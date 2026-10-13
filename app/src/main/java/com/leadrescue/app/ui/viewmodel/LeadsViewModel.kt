package com.leadrescue.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadFilter
import com.leadrescue.app.data.LeadRepository
import com.leadrescue.app.data.matchesQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeadsUiState(
    val loading: Boolean = true,
    val now: Long = System.currentTimeMillis(),
    val totalLeads: Int = 0,
    val leads: List<Lead> = emptyList(),
    val counts: Map<LeadFilter, Int> = emptyMap(),
)

class LeadsViewModel(
    handle: SavedStateHandle,
    private val repository: LeadRepository,
    ticks: Flow<Long>,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(LeadFilter.fromName(handle.get<String>("filter")))
    val filter: StateFlow<LeadFilter> = _filter.asStateFlow()

    val state: StateFlow<LeadsUiState> =
        combine(repository.observeAll(), _query, _filter, ticks) { leads, query, filter, now ->
            LeadsUiState(
                loading = false,
                now = now,
                totalLeads = leads.size,
                leads = leads.filter { filter.matches(it, now) && it.matchesQuery(query) },
                counts = LeadFilter.entries.associateWith { f -> leads.count { f.matches(it, now) } },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeadsUiState())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(value: LeadFilter) {
        _filter.value = value
    }

    fun markContacted(lead: Lead) {
        viewModelScope.launch { repository.markContacted(lead) }
    }
}
