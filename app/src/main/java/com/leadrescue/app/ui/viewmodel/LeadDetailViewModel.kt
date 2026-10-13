package com.leadrescue.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadRepository
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.data.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeadDetailUiState(
    val loading: Boolean = true,
    val lead: Lead? = null,
    val now: Long = System.currentTimeMillis(),
)

class LeadDetailViewModel(
    handle: SavedStateHandle,
    private val repository: LeadRepository,
    settings: SettingsStore,
    ticks: Flow<Long>,
) : ViewModel() {

    private val leadId: Long = handle.get<Long>("leadId") ?: -1

    val businessName: StateFlow<String> = settings.businessName

    val state: StateFlow<LeadDetailUiState> =
        combine(repository.observe(leadId), ticks) { lead, now -> LeadDetailUiState(false, lead, now) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeadDetailUiState())

    fun setStatus(status: LeadStatus) = withLead { repository.setStatus(it, status) }

    fun setFollowUp(followUpAt: Long?) = withLead { repository.setFollowUp(it, followUpAt) }

    fun markContacted() = withLead { repository.markContacted(it) }

    /** The screen closes itself when the lead stops existing. */
    fun delete() = withLead { repository.delete(it) }

    private fun withLead(action: suspend (Lead) -> Unit) {
        viewModelScope.launch {
            // Read the latest copy so quick consecutive edits never overwrite each other.
            val lead = repository.get(leadId) ?: return@launch
            action(lead)
        }
    }
}
