package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.example.game.SoundManager

@Composable
fun LevelCompleteDialog(
    score: Int,
    timeSeconds: Int,
    bestScore: Int,
    bestTime: Int,
    earnedStars: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevels: () -> Unit
) {
    var showStars by remember { mutableStateOf(0) }

    LaunchedEffect(earnedStars) {
        delay(300)
        for (i in 1..earnedStars) {
            delay(400)
            SoundManager.playStarRevealSound()
            showStars = i
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(24.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111F13)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF388E3C), Color(0xFF003300))
                    ),
                    width = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LEVEL COMPLETE!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color(0xFF81C784)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val isActive = i <= showStars
                            val scale by animateFloatAsState(
                                targetValue = if (isActive) 1f else 0.5f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (isActive) Color(0xFFFFD54F) else Color.DarkGray,
                                modifier = Modifier
                                    .size(56.dp)
                                    .scale(scale)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B3A1E), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SCORE", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha=0.6f)))
                            Text("$score", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            if (bestScore > 0) {
                                Text("Best: $bestScore", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFD54F)))
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha=0.2f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TIME", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha=0.6f)))
                            Text("${timeSeconds}s", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            if (bestTime > 0) {
                                Text("Best: ${bestTime}s", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFD54F)))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("NEXT LEVEL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("REPLAY")
                    }
                    TextButton(onClick = onLevels) {
                        Text("LEVELS", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}
