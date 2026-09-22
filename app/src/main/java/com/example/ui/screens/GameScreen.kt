package com.example.ui.screens
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.game.GameEngine
import com.example.game.Tile
import kotlin.math.abs
import com.example.ui.components.MissionFailedDialog
import com.example.ui.theme.tile_2_bg
import com.example.ui.theme.tile_2_text
import com.example.ui.theme.tile_2_border
import com.example.ui.theme.tile_4_bg
import com.example.ui.theme.tile_4_text
import com.example.ui.theme.tile_4_border
import com.example.ui.theme.tile_8_bg
import com.example.ui.theme.tile_8_text
import com.example.ui.theme.tile_8_border
import com.example.ui.theme.tile_16_bg
import com.example.ui.theme.tile_16_text
import com.example.ui.theme.tile_16_border
import com.example.ui.theme.tile_32_bg
import com.example.ui.theme.tile_32_text
import com.example.ui.theme.tile_32_border
import com.example.ui.theme.tile_64_bg
import com.example.ui.theme.tile_64_text
import com.example.ui.theme.tile_64_border
import com.example.ui.components.BannerAd

import com.example.ui.components.LevelCompleteDialog

@Composable
fun GameScreen(
    engine: GameEngine,
    score: Int,
    timeSeconds: Int,
    earnedStars: Int,
    bestScore: Int,
    bestTime: Int,
    isGameOver: Boolean,
    isLevelComplete: Boolean,
    target: Int,
    levelId: Int,
    gridTrigger: Int,
    hintDirection: GameEngine.Direction?,
    idleHintDirection: GameEngine.Direction?,
    onSwipe: (GameEngine.Direction) -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHint: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var swipeOffset by remember { mutableStateOf(Pair(0f, 0f)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CURRENT LEVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "Level $levelId",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "$target", // Target as best for now
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            val (x, y) = swipeOffset
                            if (abs(x) > abs(y)) {
                                if (x > 40) onSwipe(GameEngine.Direction.RIGHT)
                                else if (x < -40) onSwipe(GameEngine.Direction.LEFT)
                            } else {
                                if (y > 40) onSwipe(GameEngine.Direction.DOWN)
                                else if (y < -40) onSwipe(GameEngine.Direction.UP)
                            }
                            swipeOffset = Pair(0f, 0f)
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        swipeOffset = Pair(swipeOffset.first + dragAmount.x, swipeOffset.second + dragAmount.y)
                    }
                }
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val dummy = gridTrigger
                val spacing = 8.dp
                val tileSize = (maxWidth - (spacing * 4)) / 5
                
                for (y in 0 until 5) {
                    for (x in 0 until 5) {
                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .offset(
                                    x = (tileSize + spacing) * x,
                                    y = (tileSize + spacing) * y
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                val tilesToRender = mutableListOf<Pair<Tile, Boolean>>()
                for (x in 0 until 5) {
                    for (y in 0 until 5) {
                        val t = engine.grid[x][y]
                        if (t != null) {
                            tilesToRender.add(t to false)
                            if (t.isMerged) {
                                for (mt in t.mergedFrom) {
                                    tilesToRender.add(mt to true)
                                }
                            }
                        }
                    }
                }

                for ((tile, isDead) in tilesToRender.sortedBy { it.first.id }) {
                    key(tile.id) {
                        AnimatedTileView(
                            tile = tile,
                            tileSize = tileSize,
                            spacing = spacing,
                            isDead = isDead
                        )
                    }
                }

                for ((tile, isDead) in tilesToRender.sortedBy { it.first.id }) {
                    key(tile.id) {
                        AnimatedTileView(
                            tile = tile,
                            tileSize = tileSize,
                            spacing = spacing,
                            isDead = isDead
                        )
                    }
                }
            }
            
            HintOverlay(hintDirection = hintDirection)
            IdleHintOverlay(hintDirection = idleHintDirection)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val progress = (score.toFloat() / target.toFloat()).coerceIn(0f, 1f)
            val progressPercent = (progress * 100).toInt()
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(100))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100))
                )
            }
            Text(
                text = "$progressPercent% to Level ${levelId + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onUndo,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(
                onClick = onHint,
                modifier = Modifier
                    .background(
                        if (hintDirection != null) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = if (hintDirection != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onRestart,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        BannerAd()
    }

    if (isLevelComplete) {
        LevelCompleteDialog(
            score = score,
            timeSeconds = timeSeconds,
            bestScore = bestScore,
            bestTime = bestTime,
            earnedStars = earnedStars,
            onNextLevel = onNextLevel,
            onReplay = onReplay,
            onLevels = onBack
        )
    } else if (isGameOver) {
        MissionFailedDialog(score = score, target = target, onRetry = onReplay, onLevels = onBack)
    }
}

@Composable
fun AnimatedTileView(tile: Tile, tileSize: androidx.compose.ui.unit.Dp, spacing: androidx.compose.ui.unit.Dp, isDead: Boolean = false) {
    val (bgColor, textColor, borderColor) = when (tile.value) {
        2 -> Triple(tile_2_bg, tile_2_text, tile_2_border)
        4 -> Triple(tile_4_bg, tile_4_text, tile_4_border)
        8 -> Triple(tile_8_bg, tile_8_text, tile_8_border)
        16 -> Triple(tile_16_bg, tile_16_text, tile_16_border)
        32 -> Triple(tile_32_bg, tile_32_text, tile_32_border)
        else -> Triple(tile_64_bg, tile_64_text, tile_64_border)
    }

    val targetX = (tileSize + spacing) * tile.x
    val targetY = (tileSize + spacing) * tile.y
    val startX = (tileSize + spacing) * tile.prevX
    val startY = (tileSize + spacing) * tile.prevY

    var hasInitialized by remember { mutableStateOf(false) }

    val animX by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (hasInitialized || !tile.isNew) targetX else startX,
        animationSpec = tween(durationMillis = 150, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "x"
    )
    val animY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (hasInitialized || !tile.isNew) targetY else startY,
        animationSpec = tween(durationMillis = 150, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "y"
    )

    val scale = remember { androidx.compose.animation.core.Animatable(if (tile.isNew && !tile.isMerged) 0f else 1f) }
    val glowAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(tile.id) {
        hasInitialized = true
        if (tile.isNew && !tile.isMerged) {
            scale.animateTo(1f, animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
    }

    val pulseScale = remember { androidx.compose.animation.core.Animatable(1f) }
    val pulseAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(tile.isMerged) {
        if (tile.isMerged) {
            // wait for sliding tiles to arrive
            kotlinx.coroutines.delay(120)
            
            launch {
                // compress and scale up
                scale.animateTo(1.2f, animationSpec = tween(80, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                scale.animateTo(0.9f, animationSpec = tween(60, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                scale.animateTo(1f, animationSpec = tween(60, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            }
            launch {
                glowAlpha.animateTo(0.8f, animationSpec = tween(100))
                glowAlpha.animateTo(0f, animationSpec = tween(200))
            }
            launch {
                pulseScale.snapTo(1f)
                pulseAlpha.snapTo(0.6f)
                launch { pulseScale.animateTo(1.5f, animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) }
                launch { pulseAlpha.animateTo(0f, animationSpec = tween(300, easing = androidx.compose.animation.core.LinearEasing)) }
            }
        }
    }

    // Squash and stretch micro feedback during movement
    val isMovingX = kotlin.math.abs(animX.value - targetX.value) > 1f
    val isMovingY = kotlin.math.abs(animY.value - targetY.value) > 1f
    val scaleX = if (isMovingX) 1.05f else if (isMovingY) 0.95f else 1f
    val scaleY = if (isMovingY) 1.05f else if (isMovingX) 0.95f else 1f

    val finalScaleX = scaleX * scale.value * (if (isDead && !isMovingX && !isMovingY) 0f else 1f)
    val finalScaleY = scaleY * scale.value * (if (isDead && !isMovingX && !isMovingY) 0f else 1f)

    Box(
        modifier = Modifier
            .size(tileSize)
            .offset(x = animX, y = animY),
        contentAlignment = Alignment.Center
    ) {
        if (pulseAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale.value)
                    .background(Color.White.copy(alpha = pulseAlpha.value), RoundedCornerShape(12.dp))
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = finalScaleX, scaleY = finalScaleY),
            shape = RoundedCornerShape(12.dp),
            color = borderColor
        ) {
        Box(
            modifier = Modifier
                .padding(bottom = 3.dp)
                .background(bgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${tile.value}",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = textColor
                )
            )
            if (glowAlpha.value > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = glowAlpha.value), RoundedCornerShape(12.dp)))
            }
        }
    }
    }
}

@Composable
fun HintOverlay(hintDirection: GameEngine.Direction?) {
    androidx.compose.animation.AnimatedVisibility(
        visible = hintDirection != null,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "hint_bounce")
        val offset by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hint_bounce_anim"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            val arrowIcon = when (hintDirection) {
                GameEngine.Direction.UP -> Icons.Default.KeyboardArrowUp
                GameEngine.Direction.DOWN -> Icons.Default.KeyboardArrowDown
                GameEngine.Direction.LEFT -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
                GameEngine.Direction.RIGHT -> Icons.AutoMirrored.Filled.KeyboardArrowRight
                else -> null
            }
            if (arrowIcon != null) {
                val xOffset = when (hintDirection) {
                    GameEngine.Direction.LEFT, GameEngine.Direction.RIGHT -> offset
                    else -> 0f
                }
                val yOffset = when (hintDirection) {
                    GameEngine.Direction.UP, GameEngine.Direction.DOWN -> offset
                    else -> 0f
                }
                Icon(
                    imageVector = arrowIcon,
                    contentDescription = "Hint Direction",
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = xOffset.dp, y = yOffset.dp)
                )
            }
        }
    }
    }


@Composable
fun IdleHintOverlay(hintDirection: GameEngine.Direction?) {
    androidx.compose.animation.AnimatedVisibility(
        visible = hintDirection != null,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "idle_hint")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idle_hint_alpha"
        )
        val offset by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idle_hint_offset"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val arrowIcon = when (hintDirection) {
                GameEngine.Direction.UP -> Icons.Default.KeyboardArrowUp
                GameEngine.Direction.DOWN -> Icons.Default.KeyboardArrowDown
                GameEngine.Direction.LEFT -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
                GameEngine.Direction.RIGHT -> Icons.AutoMirrored.Filled.KeyboardArrowRight
                else -> null
            }
            if (arrowIcon != null) {
                val xOffset = when (hintDirection) {
                    GameEngine.Direction.LEFT, GameEngine.Direction.RIGHT -> offset
                    else -> 0f
                }
                val yOffset = when (hintDirection) {
                    GameEngine.Direction.UP, GameEngine.Direction.DOWN -> offset
                    else -> 0f
                }
                Icon(
                    imageVector = arrowIcon,
                    contentDescription = "Suggested Move",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = xOffset.dp, y = yOffset.dp)
                )
            }
        }
    }
    }

