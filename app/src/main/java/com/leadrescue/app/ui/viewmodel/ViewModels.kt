package com.leadrescue.app.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.leadrescue.app.AppContainer
import com.leadrescue.app.LeadRescueApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** One factory that knows how to build every screen's ViewModel. */
object AppViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { DashboardViewModel(container().repository, container().settings, clockTicks()) }
        initializer { LeadsViewModel(createSavedStateHandle(), container().repository, clockTicks()) }
        initializer { LeadFormViewModel(createSavedStateHandle(), container().repository) }
        initializer { LeadDetailViewModel(createSavedStateHandle(), container().repository, container().settings, clockTicks()) }
    }

    private fun CreationExtras.container(): AppContainer =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LeadRescueApp).container
}

/** Emits the current time every minute so "due today" / "overdue" labels stay correct while the app is open. */
fun clockTicks(): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(60_000)
    }
}
