package com.leadrescue.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    /** Leads with the nearest follow-up first; leads without a follow-up date go last. */
    @Query(
        """
        SELECT * FROM leads
        ORDER BY CASE WHEN followUpAt IS NULL THEN 1 ELSE 0 END, followUpAt ASC, updatedAt DESC
        """
    )
    fun observeAll(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :id")
    fun observeById(id: Long): Flow<Lead?>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun getById(id: Long): Lead?

    @Query("SELECT * FROM leads WHERE status IN ('NEW', 'CONTACTED', 'QUOTED') AND followUpAt IS NOT NULL")
    suspend fun getOpenWithFollowUp(): List<Lead>

    @Insert
    suspend fun insert(lead: Lead): Long

    @Update
    suspend fun update(lead: Lead)

    @Delete
    suspend fun delete(lead: Lead)
}
