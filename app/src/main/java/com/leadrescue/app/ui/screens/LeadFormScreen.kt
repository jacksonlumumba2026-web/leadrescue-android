package com.leadrescue.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leadrescue.app.data.LeadSource
import com.leadrescue.app.data.LeadStatus
import com.leadrescue.app.ui.components.AppCard
import com.leadrescue.app.ui.components.FollowUpDateDialog
import com.leadrescue.app.ui.components.FollowUpTimeDialog
import com.leadrescue.app.ui.theme.Coral
import com.leadrescue.app.ui.theme.color
import com.leadrescue.app.ui.viewmodel.AppViewModelFactory
import com.leadrescue.app.ui.viewmodel.LeadFormViewModel
import com.leadrescue.app.util.FollowUpDates
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadFormScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: LeadFormViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit lead" else "New lead") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    TextButton(onClick = { viewModel.save(onSaved) }, enabled = !viewModel.saving && !viewModel.loading) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        if (viewModel.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FormSection("Customer") {
                FormField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = "Customer name",
                    icon = Icons.Filled.Person,
                    error = viewModel.nameError.takeIf { viewModel.showErrors },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                )
                FormField(
                    value = viewModel.phone,
                    onValueChange = { viewModel.phone = it },
                    label = "Phone number",
                    placeholder = "0712 345 678",
                    icon = Icons.Filled.Phone,
                    error = viewModel.phoneError.takeIf { viewModel.showErrors },
                    hint = viewModel.phonePreview,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                )
            }

            FormSection("Inquiry") {
                FormField(
                    value = viewModel.product,
                    onValueChange = { viewModel.product = it },
                    label = "Product or service requested",
                    placeholder = "e.g. 2 bags of cement, braids, website",
                    icon = Icons.Filled.ShoppingCart,
                    error = viewModel.productError.takeIf { viewModel.showErrors },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                )
                FormField(
                    value = viewModel.valueText,
                    onValueChange = { input -> viewModel.valueText = input.filter { it.isDigit() || it == ',' }.take(15) },
                    label = "Estimated value (optional)",
                    prefix = "KSh ",
                    placeholder = "12,500",
                    error = viewModel.valueError.takeIf { viewModel.showErrors },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                )
                Label("Where did they find you?")
                ChipGroup(
                    options = LeadSource.entries,
                    selected = viewModel.source,
                    label = { it.label },
                    onSelect = { viewModel.source = it },
                )
            }

            FormSection("Status") {
                ChipGroup(
                    options = LeadStatus.entries,
                    selected = viewModel.status,
                    label = { it.label },
                    onSelect = { viewModel.status = it },
                    selectedColor = { it.color() },
                )
            }

            FormSection("Next follow-up") {
                FollowUpEditor(
                    date = viewModel.followUpDate,
                    onDateChange = { viewModel.followUpDate = it },
                    timeLabel = FollowUpDates.formatTime(FollowUpDates.toMillis(LocalDate.now(), viewModel.followUpTime)),
                    onPickTime = { viewModel.followUpTime = it },
                    currentTime = viewModel.followUpTime,
                    summary = viewModel.followUpAt?.let { FollowUpDates.formatDayAndTime(it, System.currentTimeMillis()) },
                    warnPast = viewModel.followUpInPast,
                )
            }

            FormSection("Notes") {
                OutlinedTextField(
                    value = viewModel.notes,
                    onValueChange = { viewModel.notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Budget, preferred colour, delivery location…") },
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }

            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = !viewModel.saving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(if (viewModel.isEditing) "Save changes" else "Save lead", style = MaterialTheme.typography.titleMedium)
            }
            if (viewModel.showErrors && (viewModel.nameError ?: viewModel.phoneError ?: viewModel.productError ?: viewModel.valueError) != null) {
                Text(
                    "Please fix the highlighted fields.",
                    color = Coral,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    icon: ImageVector? = null,
    prefix: String? = null,
    error: String? = null,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null) } },
        prefix = prefix?.let { { Text(it) } },
        isError = error != null,
        supportingText = (error ?: hint)?.let { { Text(it) } },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    selectedColor: ((T) -> androidx.compose.ui.graphics.Color)? = null,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val color = selectedColor?.invoke(option) ?: MaterialTheme.colorScheme.primary
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.16f),
                    selectedLabelColor = color,
                ),
            )
        }
    }
}

/**
 * Quick choices (Tomorrow, In 3 days, Next week, None) plus full date and time pickers.
 * Shared by the form and the lead details screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FollowUpEditor(
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    timeLabel: String,
    currentTime: java.time.LocalTime,
    onPickTime: (java.time.LocalTime) -> Unit,
    summary: String?,
    warnPast: Boolean,
) {
    var pickingDate by remember { mutableStateOf(false) }
    var pickingTime by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val quick = listOf(
        "Today" to today,
        "Tomorrow" to today.plusDays(1),
        "In 3 days" to today.plusDays(3),
        "Next week" to today.plusWeeks(1),
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = null,
            tint = if (date != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            summary ?: "No follow-up planned",
            style = MaterialTheme.typography.titleMedium,
        )
    }
    if (warnPast) {
        Text(
            "This time has already passed. Pick a later time to get a reminder.",
            style = MaterialTheme.typography.bodySmall,
            color = Coral,
        )
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        quick.forEach { (label, day) ->
            FilterChip(
                selected = date == day,
                onClick = { onDateChange(day) },
                label = { Text(label) },
                shape = MaterialTheme.shapes.small,
            )
        }
        FilterChip(
            selected = date == null,
            onClick = { onDateChange(null) },
            label = { Text("None") },
            shape = MaterialTheme.shapes.small,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = { pickingDate = true }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Pick date")
        }
        OutlinedButton(onClick = { pickingTime = true }, enabled = date != null, modifier = Modifier.weight(1f)) {
            Text(timeLabel)
        }
    }

    if (pickingDate) {
        FollowUpDateDialog(
            initial = date,
            onDismiss = { pickingDate = false },
            onPicked = { onDateChange(it); pickingDate = false },
        )
    }
    if (pickingTime) {
        FollowUpTimeDialog(
            initial = currentTime,
            onDismiss = { pickingTime = false },
            onPicked = { onPickTime(it); pickingTime = false },
        )
    }
}
