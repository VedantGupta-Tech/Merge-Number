package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GameLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    var isReadyToProceed by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing)
        )
        delay(200)
        onNavigateToHome()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_bg_particles")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val tile1Y by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "t1"
    )
    val tile2Y by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
        label = "t2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D3268),
                        Color(0xFF001738),
                        Color(0xFF000E24)
                    )
                )
            )
            .clickable { onNavigateToHome() },
        contentAlignment = Alignment.Center
    ) {
        // Decorative floating background tiles (subtle ambient look)
        // Top Left floating "4"
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 28.dp, top = 70.dp)
                .offset(y = tile1Y.dp)
                .size(44.dp)
                .alpha(0.28f)
                .background(Color(0xFF8E24AA), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("4", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Top Right floating "32"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 32.dp, top = 85.dp)
                .offset(y = tile2Y.dp)
                .size(48.dp)
                .alpha(0.28f)
                .background(Color(0xFFE53935), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("32", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Bottom Left floating "2"
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 140.dp)
                .offset(y = tile2Y.dp)
                .size(46.dp)
                .alpha(0.25f)
                .background(Color(0xFF43A047), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Bottom Right floating "64"
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 150.dp)
                .offset(y = tile1Y.dp)
                .size(52.dp)
                .alpha(0.25f)
                .background(Color(0xFFD81B60), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("64", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Ambient radial glow behind the main logo
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD54F).copy(alpha = 0.25f),
                            Color(0xFF1E88E5).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main Center Animated Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            GameLogo(animateEntrance = true)

            Spacer(modifier = Modifier.height(36.dp))

            // Animated Loading Bar
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color(0xFF002244))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFCA28),
                                    Color(0xFFFF8F00),
                                    Color(0xFFFFD54F)
                                )
                            ),
                            RoundedCornerShape(100)
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "LOADING LEVELS...",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFFCA28).copy(alpha = 0.85f)
                )
            )
        }
    }
}
