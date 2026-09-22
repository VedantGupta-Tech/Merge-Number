package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LevelData
import com.example.ui.components.BannerAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    levels: List<LevelData>,
    onBackClick: () -> Unit,
    onLevelClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Level") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BannerAd()
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(levels) { level ->
                LevelTile(level = level, onClick = {
                    if (level.isUnlocked) {
                        onLevelClick(level.id)
                    }
                })
            }
        }
    }
}

@Composable
fun LevelTile(level: LevelData, onClick: () -> Unit) {
    val containerColor = if (level.isCompleted) {
        MaterialTheme.colorScheme.primary
    } else if (level.isUnlocked) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (level.isCompleted) {
        MaterialTheme.colorScheme.onPrimary
    } else if (level.isUnlocked) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = level.isUnlocked, onClick = onClick)
            .alpha(if (level.isUnlocked) 1f else 0.6f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (level.isUnlocked) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${level.id}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    )
                    if (level.isCompleted) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            for (i in 1..3) {
                                val isFilled = i <= level.stars
                                Text(
                                    text = if (isFilled) "★" else "☆",
                                    color = if (isFilled) androidx.compose.ui.graphics.Color(0xFFFFD54F) else contentColor.copy(alpha=0.5f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = contentColor
                )
            }
        }
    }
}
