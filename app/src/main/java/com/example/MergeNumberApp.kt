package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.GameDatabase
import com.example.data.GameRepository

import com.example.data.MIGRATION_2_3
import com.example.data.MIGRATION_3_4

class MergeNumberApp : Application() {
    lateinit var repository: GameRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            this,
            GameDatabase::class.java,
            "merge_number_db"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
        repository = GameRepository(database.levelDao())
    }
}
