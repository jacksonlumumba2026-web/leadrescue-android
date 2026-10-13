package com.leadrescue.app

import android.content.Context
import com.leadrescue.app.data.LeadDatabase
import com.leadrescue.app.data.LeadRepository
import com.leadrescue.app.data.SettingsStore
import com.leadrescue.app.reminders.ReminderScheduler

/** Creates the app's shared objects once. A simple alternative to a dependency-injection library. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: LeadDatabase by lazy { LeadDatabase.create(appContext) }
    val reminders: ReminderScheduler by lazy { ReminderScheduler(appContext) }
    val repository: LeadRepository by lazy { LeadRepository(database.leadDao(), reminders) }
    val settings: SettingsStore by lazy { SettingsStore(appContext) }
}
