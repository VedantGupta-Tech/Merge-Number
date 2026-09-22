package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameLogo(
    modifier: Modifier = Modifier,
    animateEntrance: Boolean = false
) {
    // Ambient floating animations
    val infiniteTransition = rememberInfiniteTransition(label = "logo_ambient")
    
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Entrance animation states
    var startAnim by remember { mutableStateOf(!animateEntrance) }
    
    LaunchedEffect(Unit) {
        if (animateEntrance) {
            startAnim = true
        }
    }

    val leftTileOffset by animateFloatAsState(
        targetValue = if (startAnim) -64f else -250f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "left_tile_offset"
    )

    val rightTileOffset by animateFloatAsState(
        targetValue = if (startAnim) 64f else 250f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "right_tile_offset"
    )

    val centerScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "center_scale"
    )

    val boardScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "board_scale"
    )

    val boardAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(500, delayMillis = 200),
        label = "board_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        // Golden glowing aura behind center tile
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 35.dp)
                .size(140.dp)
                .scale(glowPulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD54F).copy(alpha = 0.45f * glowPulse),
                            Color(0xFFFF8F00).copy(alpha = 0.2f * glowPulse),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Floating tiles area
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 35.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Left Tile 8
            LogoTile(
                value = "8",
                bgColor = Color(0xFF1E88E5),
                textColor = Color.White,
                rotation = -14f,
                modifier = Modifier
                    .size(62.dp)
                    .offset(x = leftTileOffset.dp, y = 22.dp)
            )
            
            // Right Tile 8
            LogoTile(
                value = "8",
                bgColor = Color(0xFF1E88E5),
                textColor = Color.White,
                rotation = 14f,
                modifier = Modifier
                    .size(62.dp)
                    .offset(x = rightTileOffset.dp, y = 22.dp)
            )

            // Center Tile 16 with Crown
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .scale(centerScale),
                contentAlignment = Alignment.Center
            ) {
                // Crown Icon with sparkling color
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFE082),
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-26).dp)
                        .shadow(8.dp, CircleShape)
                )
                
                LogoTile(
                    value = "16",
                    bgColor = Color(0xFFFFB300),
                    textColor = Color.White,
                    rotation = 0f,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Wood Board with "MERGE NUMBER" text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .scale(boardScale)
                .alpha(boardAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wood board background container
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF8D6E63), Color(0xFF4E342E))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(2.5.dp, Color(0xFF3E2723), RoundedCornerShape(20.dp))
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MERGE",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFCA28),
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "NUMBER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF3E2723), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RELAX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082), letterSpacing = 1.sp)
                        Text("•", fontSize = 10.sp, color = Color(0xFFFFCA28))
                        Text("THINK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082), letterSpacing = 1.sp)
                        Text("•", fontSize = 10.sp, color = Color(0xFFFFCA28))
                        Text("MERGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082), letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LogoTile(
    value: String,
    bgColor: Color,
    textColor: Color,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .rotate(rotation)
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgColor.copy(alpha = 0.9f), bgColor)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(2.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontSize = if (value.length > 1) 36.sp else 28.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

