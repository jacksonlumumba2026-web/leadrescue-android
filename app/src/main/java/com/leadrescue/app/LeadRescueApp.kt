package com.leadrescue.app

import android.app.Application
import com.leadrescue.app.reminders.Notifications

class LeadRescueApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannel(this)
        container.reminders.scheduleDailyDigest()
    }
}
