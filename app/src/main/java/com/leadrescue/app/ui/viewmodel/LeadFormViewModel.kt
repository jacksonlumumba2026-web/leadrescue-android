package com.leadrescue.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leadrescue.app.data.Lead
import com.leadrescue.app.data.LeadRepository
import com.leadrescue.app.data.LeadSource
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.util.FollowUpDates
import com.leadrescue.app.util.KenyanPhone
import com.leadrescue.app.util.Ksh
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** Holds the Add / Edit lead form. Pass a "leadId" nav argument to edit an existing lead. */
class LeadFormViewModel(
    handle: SavedStateHandle,
    private val repository: LeadRepository,
) : ViewModel() {

    val leadId: Long? = handle.get<Long>("leadId")?.takeIf { it > 0 }
    val isEditing: Boolean get() = leadId != null

    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var product by mutableStateOf("")
    var source by mutableStateOf(LeadSource.WHATSAPP)
    var valueText by mutableStateOf("")
    var status by mutableStateOf(LeadStatus.NEW)
    var notes by mutableStateOf("")
    var followUpDate by mutableStateOf<LocalDate?>(LocalDate.now().plusDays(1))
    var followUpTime by mutableStateOf(FollowUpDates.DEFAULT_TIME)

    /** Errors are only shown after the user tries to save once. */
    var showErrors by mutableStateOf(false)
        private set
    var loading by mutableStateOf(leadId != null)
        private set
    var saving by mutableStateOf(false)
        private set

    private var original: Lead? = null

    init {
        if (leadId != null) {
            viewModelScope.launch {
                repository.get(leadId)?.let { fill(it) }
                loading = false
            }
        }
    }

    private fun fill(lead: Lead) {
        original = lead
        name = lead.name
        phone = KenyanPhone.format(lead.phone)
        product = lead.product
        source = lead.source
        valueText = lead.valueKsh?.toString() ?: ""
        status = lead.status
        notes = lead.notes
        followUpDate = lead.followUpAt?.let { FollowUpDates.toDate(it) }
        followUpTime = lead.followUpAt?.let { FollowUpDates.toLocalTime(it) } ?: FollowUpDates.DEFAULT_TIME
    }

    val nameError: String?
        get() = if (name.isBlank()) "Enter the customer's name" else null

    val phoneError: String?
        get() = when {
            phone.isBlank() -> "Enter a phone number"
            !KenyanPhone.isValid(phone) -> "Use a Kenyan mobile like 0712 345 678 (or +country code for other countries)"
            else -> null
        }

    val productError: String?
        get() = if (product.isBlank()) "What did they ask about?" else null

    val valueError: String?
        get() = if (valueText.isNotBlank() && Ksh.parse(valueText) == null) "Enter an amount in KSh, e.g. 12500" else null

    /** Friendly preview of how the phone will be saved, shown under the field. */
    val phonePreview: String?
        get() = KenyanPhone.normalize(phone)?.let { "WhatsApp: ${KenyanPhone.format(it)}" }

    val followUpAt: Long?
        get() = followUpDate?.let { FollowUpDates.toMillis(it, followUpTime) }

    val followUpInPast: Boolean
        get() = followUpAt?.let { it < System.currentTimeMillis() } ?: false

    private val isValid: Boolean
        get() = nameError == null && phoneError == null && productError == null && valueError == null

    /** Saves the lead and calls [onSaved] with its id. Does nothing if the form has errors. */
    fun save(onSaved: (Long) -> Unit) {
        showErrors = true
        if (!isValid || saving) return
        saving = true
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val base = original
            val lead = (base ?: Lead(name = "", phone = "", product = "", source = source, createdAt = now, updatedAt = now))
                .copy(
                    name = name.trim(),
                    phone = KenyanPhone.normalize(phone)!!,
                    product = product.trim(),
                    source = source,
                    valueKsh = Ksh.parse(valueText),
                    status = status,
                    notes = notes.trim(),
                    followUpAt = followUpAt,
                )
            val id = if (base == null) repository.add(lead) else { repository.save(lead); base.id }
            saving = false
            onSaved(id)
        }
    }
}
