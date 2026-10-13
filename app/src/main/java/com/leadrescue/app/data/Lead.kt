package com.leadrescue.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One potential customer.
 * Times are epoch milliseconds. [phone] is always normalised (e.g. "+254712345678").
 */
@Entity(
    tableName = "leads",
    indices = [Index("status"), Index("followUpAt")],
)
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val product: String,
    val source: LeadSource,
    val notes: String = "",
    val status: LeadStatus = LeadStatus.NEW,
    val followUpAt: Long? = null,
    val valueKsh: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val lastContactedAt: Long? = null,
)
