package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelData(
    @PrimaryKey val id: Int,
    val target: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val highScore: Int = 0,
    val bestTime: Int = 0,
    val gridWidth: Int = 5,
    val gridHeight: Int = 5,
    val availableHints: Int = 5
) {
    // Configurable thresholds per level
    val threeStarTime: Int
        get() = 60 + (id * 15) // Example: 75s, 90s, etc.
        
    val twoStarTime: Int
        get() = 120 + (id * 30) // Example: 150s, 180s, etc.
        
    val threeStarScore: Int
        get() = target * 4
        
    val twoStarScore: Int
        get() = target * 2
}
