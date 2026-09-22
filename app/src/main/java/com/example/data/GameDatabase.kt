package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LevelData::class], version = 4, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE levels ADD COLUMN bestTime INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE levels ADD COLUMN availableHints INTEGER NOT NULL DEFAULT 5")
    }
}
