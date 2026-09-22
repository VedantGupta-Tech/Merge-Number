package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    private val COINS_KEY = intPreferencesKey("coins")
    private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
    private val MUSIC_VOLUME_KEY = floatPreferencesKey("music_volume")
    private val SFX_ENABLED_KEY = booleanPreferencesKey("sfx_enabled")
    private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
    private val FREE_HINTS_USED_KEY = intPreferencesKey("free_hints_used")
    private val GAMEPLAYS_COMPLETED_KEY = intPreferencesKey("gameplays_completed")
    
    val coins: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COINS_KEY] ?: 0
    }
    
    val freeHintsUsed: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FREE_HINTS_USED_KEY] ?: 0
    }

    val gameplaysCompleted: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[GAMEPLAYS_COMPLETED_KEY] ?: 0
    }
    
    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_ENABLED_KEY] ?: true
    }

    val musicVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_VOLUME_KEY] ?: 0.5f
    }

    val sfxEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SFX_ENABLED_KEY] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED_KEY] ?: true
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[COINS_KEY] ?: 0
            preferences[COINS_KEY] = current + amount
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME_KEY] = volume
        }
    }

    suspend fun setSfxEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SFX_ENABLED_KEY] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }

    suspend fun incrementFreeHintsUsed() {
        context.dataStore.edit { preferences ->
            val current = preferences[FREE_HINTS_USED_KEY] ?: 0
            preferences[FREE_HINTS_USED_KEY] = current + 1
        }
    }

    suspend fun incrementGameplaysCompleted() {
        context.dataStore.edit { preferences ->
            val current = preferences[GAMEPLAYS_COMPLETED_KEY] ?: 0
            preferences[GAMEPLAYS_COMPLETED_KEY] = current + 1
        }
    }
}
