package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.UserPreferences
import com.example.ui.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.game.SoundManager
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}
        SoundManager.init(this)
        val app = application as MergeNumberApp
        val userPrefs = UserPreferences(this)
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: GameViewModel = viewModel(
                        factory = GameViewModel.Factory(app.repository, userPrefs)
                    )
                    
                    var showSettings by remember { mutableStateOf(false) }
                    val musicEnabled by viewModel.musicEnabled.collectAsState()
                    val musicVolume by viewModel.musicVolume.collectAsState()
                    val sfxEnabled by viewModel.sfxEnabled.collectAsState()
                    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
                    
                    // Start playing BGM as soon as app opens (after Splash or during)
                    LaunchedEffect(Unit) {
                        SoundManager.playBgm()
                    }
                    
                    if (showSettings) {
                        SettingsDialog(
                            musicEnabled = musicEnabled,
                            musicVolume = musicVolume,
                            sfxEnabled = sfxEnabled,
                            vibrationEnabled = vibrationEnabled,
                            onMusicEnabledChange = { viewModel.setMusicEnabled(it) },
                            onMusicVolumeChange = { viewModel.setMusicVolume(it) },
                            onSfxEnabledChange = { viewModel.setSfxEnabled(it) },
                            onVibrationEnabledChange = { viewModel.setVibrationEnabled(it) },
                            onDismiss = { showSettings = false }
                        )
                    }

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("home") {
                            val coins by viewModel.coins.collectAsState()
                            val levels by viewModel.allLevels.collectAsState()
                            
                            HomeScreen(
                                coins = coins,
                                onPlayClick = {
                                    navController.navigate("levels")
                                },
                                onLevelsClick = {
                                    navController.navigate("levels")
                                },
                                onSettingsClick = {
                                    showSettings = true
                                }
                            )
                        }
                        composable("levels") {
                            val levels by viewModel.allLevels.collectAsState()
                            LevelSelectionScreen(
                                levels = levels,
                                onBackClick = { navController.popBackStack() },
                                onLevelClick = { levelId ->
                                    viewModel.startLevel(levelId)
                                    navController.navigate("game")
                                }
                            )
                        }
                        composable("game") {
                            val uiState by viewModel.uiState.collectAsState()
                            val gridTrigger by viewModel.gridUpdateTrigger.collectAsState()
                            val level = uiState.currentLevel
                            
                            val freeHintsUsed by viewModel.freeHintsUsed.collectAsState()
                            val gameplaysCompleted by viewModel.gameplaysCompleted.collectAsState()

                            LaunchedEffect(Unit) {
                                com.example.game.AdManager.loadInterstitialAd(this@MainActivity)
                                com.example.game.AdManager.loadRewardedAd(this@MainActivity)
                            }

                            fun handleGameplayTransition(action: () -> Unit) {
                                viewModel.incrementGameplaysCompleted()
                                if ((gameplaysCompleted + 1) % 3 == 0) {
                                    com.example.game.AdManager.showInterstitialAd(this@MainActivity) {
                                        action()
                                    }
                                } else {
                                    action()
                                }
                            }
                            
                            if (level != null) {
                                GameScreen(
                                    engine = viewModel.engine,
                                    score = uiState.score,
                                    timeSeconds = uiState.timeSeconds,
                                    earnedStars = uiState.earnedStars,
                                    bestScore = level.highScore,
                                    bestTime = level.bestTime,
                                    isGameOver = uiState.isGameOver,
                                    isLevelComplete = uiState.isLevelComplete,
                                    target = level.target,
                                    levelId = level.id,
                                    gridTrigger = gridTrigger,
                                    hintDirection = uiState.hintDirection,
                                    idleHintDirection = uiState.idleHintDirection,
                                    onSwipe = { direction -> viewModel.onSwipe(direction) },
                                    onUndo = { viewModel.undo() },
                                    onRestart = { viewModel.restart() },
                                    onBack = { 
                                        if (uiState.isLevelComplete || uiState.isGameOver) {
                                            handleGameplayTransition { navController.popBackStack() }
                                        } else {
                                            navController.popBackStack()
                                        }
                                    },
                                    onNextLevel = {
                                        handleGameplayTransition { viewModel.startLevel(level.id + 1) }
                                    },
                                    onReplay = {
                                        if (uiState.isLevelComplete || uiState.isGameOver) {
                                            handleGameplayTransition { viewModel.startLevel(level.id) }
                                        } else {
                                            viewModel.startLevel(level.id)
                                        }
                                    },
                                    onHint = { 
                                        if (level.availableHints > 0) {
                                            viewModel.useHint()
                                        } else {
                                            com.example.game.AdManager.showRewardedAd(
                                                activity = this@MainActivity,
                                                onRewardEarned = {
                                                    viewModel.addHintsAndUseOne()
                                                },
                                                onAdFailedOrDismissed = {
                                                    android.widget.Toast.makeText(this@MainActivity, "Reward unavailable. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    },
                                    onSettingsClick = { showSettings = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.resumeBgm()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}
