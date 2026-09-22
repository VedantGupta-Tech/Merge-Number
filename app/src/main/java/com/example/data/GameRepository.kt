package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val levelDao: LevelDao) {
    val allLevels: Flow<List<LevelData>> = levelDao.getAllLevels()

    suspend fun getLevel(id: Int): LevelData? = levelDao.getLevelById(id)

    suspend fun updateLevel(level: LevelData) {
        levelDao.updateLevel(level)
    }

    suspend fun unlockLevel(id: Int) {
        levelDao.unlockLevel(id)
    }

    suspend fun initializeLevels() {
        val count = levelDao.getAllLevelsSync()
        if (count >= 5000) return
        
        val levels = (1..5000).map { id ->
            // Change target every 5 levels, capping at 16 shl 21 (33,554,432) to prevent overflow
            // and remain theoretically possible on a 5x5 grid
            val shift = minOf((id - 1) / 5, 21)
            val target = 16 shl shift
            
            LevelData(
                id = id,
                target = target,
                isUnlocked = id == 1
            )
        }
        levelDao.insertLevels(levels)
    }
}
