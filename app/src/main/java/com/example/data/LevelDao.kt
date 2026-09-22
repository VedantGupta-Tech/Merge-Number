package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY id ASC")
    fun getAllLevels(): Flow<List<LevelData>>
    
    @Query("SELECT COUNT(*) FROM levels")
    suspend fun getAllLevelsSync(): Int

    @Query("SELECT * FROM levels WHERE id = :id")
    suspend fun getLevelById(id: Int): LevelData?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLevels(levels: List<LevelData>)

    @Update
    suspend fun updateLevel(level: LevelData)
    
    @Query("UPDATE levels SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockLevel(id: Int)
}
