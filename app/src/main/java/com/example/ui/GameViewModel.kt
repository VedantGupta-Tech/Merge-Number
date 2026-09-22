package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.LevelData
import com.example.data.UserPreferences
import com.example.game.GameEngine
import com.example.game.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class GameUiState(
    val currentLevel: LevelData? = null,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val coins: Int = 0,
    val timeSeconds: Int = 0,
    val earnedStars: Int = 0,
    val hintDirection: GameEngine.Direction? = null,
    val idleHintDirection: GameEngine.Direction? = null
)

class GameViewModel(
    private val repository: GameRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private var timerJob: Job? = null
    private var idleTimerJob: Job? = null

    private fun resetIdleTimer() {
        idleTimerJob?.cancel()
        _uiState.update { it.copy(idleHintDirection = null) }
        
        if (_uiState.value.isGameOver || _uiState.value.isLevelComplete) return

        idleTimerJob = viewModelScope.launch {
            delay(12000) // 12 seconds
            if (!_uiState.value.isGameOver && !_uiState.value.isLevelComplete) {
                val bestDirection = engine.getHint()
                if (bestDirection != null) {
                    _uiState.update { it.copy(idleHintDirection = bestDirection) }
                    delay(3000) // Hide after 3 seconds
                    _uiState.update { it.copy(idleHintDirection = null) }
                }
            }
        }
    }

    val allLevels: StateFlow<List<LevelData>> = repository.allLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coins: StateFlow<Int> = userPreferences.coins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val musicEnabled: StateFlow<Boolean> = userPreferences.musicEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val musicVolume: StateFlow<Float> = userPreferences.musicVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val sfxEnabled: StateFlow<Boolean> = userPreferences.sfxEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationEnabled: StateFlow<Boolean> = userPreferences.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val freeHintsUsed: StateFlow<Int> = userPreferences.freeHintsUsed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val gameplaysCompleted: StateFlow<Int> = userPreferences.gameplaysCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val engine = GameEngine(5, 5)
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    // Track grid updates for recomposition
    private val _gridUpdateTrigger = MutableStateFlow(0)
    val gridUpdateTrigger: StateFlow<Int> = _gridUpdateTrigger

    init {
        viewModelScope.launch {
            repository.initializeLevels()
        }
        viewModelScope.launch {
            combine(musicEnabled, musicVolume, sfxEnabled, vibrationEnabled) { m, v, s, vib ->
                SoundManager.updateSettings(m, v, s, vib)
            }.collect {}
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setMusicEnabled(enabled) }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch { userPreferences.setMusicVolume(volume) }
    }

    fun setSfxEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setSfxEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setVibrationEnabled(enabled) }
    }

    fun incrementFreeHintsUsed() {
        viewModelScope.launch { userPreferences.incrementFreeHintsUsed() }
    }

    fun incrementGameplaysCompleted() {
        viewModelScope.launch { userPreferences.incrementGameplaysCompleted() }
    }

    fun startLevel(levelId: Int) {
        viewModelScope.launch {
            val level = repository.getLevel(levelId)
            _uiState.update { it.copy(
                currentLevel = level,
                isGameOver = false,
                isLevelComplete = false,
                score = 0,
                timeSeconds = 0,
                earnedStars = 0
            ) }
            engine.reset()
            _gridUpdateTrigger.value++
            startTimer()
            resetIdleTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isGameOver && !_uiState.value.isLevelComplete) {
                    _uiState.update { it.copy(timeSeconds = it.timeSeconds + 1) }
                } else {
                    break
                }
            }
        }
    }

    fun onSwipe(direction: GameEngine.Direction) {
        if (_uiState.value.isGameOver || _uiState.value.isLevelComplete) return

        val moved = engine.move(direction)
        if (moved) {
            SoundManager.playMergeSound()
            _uiState.update { it.copy(score = engine.score, hintDirection = null, idleHintDirection = null) }
            _gridUpdateTrigger.value++
            checkGameState()
            resetIdleTimer()
        }
    }

    fun undo() {
        if (engine.undo()) {
            _uiState.update { it.copy(score = engine.score, isGameOver = false, hintDirection = null, idleHintDirection = null) }
            _gridUpdateTrigger.value++
            resetIdleTimer()
        }
    }

    fun restart() {
        engine.reset()
        _uiState.update { it.copy(score = 0, isGameOver = false, isLevelComplete = false, timeSeconds = 0, earnedStars = 0, hintDirection = null, idleHintDirection = null) }
        _gridUpdateTrigger.value++
        startTimer()
        resetIdleTimer()
    }

    fun useHint() {
        if (_uiState.value.isGameOver || _uiState.value.isLevelComplete) return
        val level = _uiState.value.currentLevel ?: return
        
        if (level.availableHints <= 0) return

        val bestDirection = engine.getHint()
        if (bestDirection != null) {
            _uiState.update { it.copy(hintDirection = bestDirection) }
            SoundManager.playHintSound()
            
            // Deduct 1 hint from current level
            viewModelScope.launch {
                val updatedLevel = level.copy(availableHints = level.availableHints - 1)
                repository.updateLevel(updatedLevel)
                _uiState.update { it.copy(currentLevel = updatedLevel) }
            }

            // Clear hint after 2 seconds
            viewModelScope.launch {
                delay(2000)
                _uiState.update { it.copy(hintDirection = null) }
            }
        }
    }

    fun addHintsAndUseOne() {
        if (_uiState.value.isGameOver || _uiState.value.isLevelComplete) return
        val level = _uiState.value.currentLevel ?: return
        
        val bestDirection = engine.getHint()
        if (bestDirection != null) {
            _uiState.update { it.copy(hintDirection = bestDirection) }
            SoundManager.playHintSound()
            
            // Add 5 hints, but use 1 immediately (so +4 net)
            viewModelScope.launch {
                val updatedLevel = level.copy(availableHints = level.availableHints + 4)
                repository.updateLevel(updatedLevel)
                _uiState.update { it.copy(currentLevel = updatedLevel) }
            }

            // Clear hint after 2 seconds
            viewModelScope.launch {
                delay(2000)
                _uiState.update { it.copy(hintDirection = null) }
            }
        }
    }

    private fun checkGameState() {
        val level = _uiState.value.currentLevel ?: return
        
        if (engine.hasReachedTarget(level.target)) {
            val earnedStars = calculateStars(level, engine.score, _uiState.value.timeSeconds)
            if (earnedStars == 3) {
                SoundManager.playThreeStarWinSound()
            } else {
                SoundManager.playWinSound()
            }
            
            _uiState.update { it.copy(isLevelComplete = true, earnedStars = earnedStars) }
            timerJob?.cancel()
            
            viewModelScope.launch {
                val nextLevelId = level.id + 1
                repository.unlockLevel(nextLevelId)
                
                val currentBestTime = if (level.bestTime == 0) _uiState.value.timeSeconds else minOf(level.bestTime, _uiState.value.timeSeconds)
                val updatedLevel = level.copy(
                    isCompleted = true,
                    highScore = maxOf(level.highScore, engine.score),
                    bestTime = currentBestTime,
                    stars = maxOf(level.stars, earnedStars)
                )
                repository.updateLevel(updatedLevel)
                userPreferences.addCoins(50 * earnedStars) // Reward for completion
            }
        } else if (!engine.hasPossibleMoves()) {
            SoundManager.playGameOverSound()
            timerJob?.cancel()
            _uiState.update { it.copy(isGameOver = true) }
        }
    }

    private fun calculateStars(level: LevelData, score: Int, timeSeconds: Int): Int {
        return when {
            score >= level.threeStarScore && timeSeconds <= level.threeStarTime -> 3
            score >= level.twoStarScore && timeSeconds <= level.twoStarTime -> 2
            else -> 1
        }
    }

    class Factory(
        private val repository: GameRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository, userPreferences) as T
        }
    }
}
