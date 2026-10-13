package com.leadrescue.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Lead::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LeadDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao

    companion object {
        fun create(context: Context): LeadDatabase =
            Room.databaseBuilder(context.applicationContext, LeadDatabase::class.java, "leadrescue.db")
                .build()
    }
}
