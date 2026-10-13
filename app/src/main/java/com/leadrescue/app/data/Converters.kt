package com.leadrescue.app.data

import androidx.room.TypeConverter

/** Stores enums as their names so the database stays readable and stable. */
class Converters {
    @TypeConverter
    fun statusToString(status: LeadStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): LeadStatus =
        LeadStatus.entries.firstOrNull { it.name == value } ?: LeadStatus.NEW

    @TypeConverter
    fun sourceToString(source: LeadSource): String = source.name

    @TypeConverter
    fun stringToSource(value: String): LeadSource =
        LeadSource.entries.firstOrNull { it.name == value } ?: LeadSource.OTHER
}
