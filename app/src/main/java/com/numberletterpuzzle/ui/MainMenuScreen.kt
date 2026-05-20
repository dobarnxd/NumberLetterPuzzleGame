package com.numberletterpuzzle.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onGame: () -> Unit,
    onPuzzleEditor: () -> Unit,
    onStatistics: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedMenuItem(delayMs = 0) {
                Text(
                    text = "Számrejtvény",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedMenuItem(delayMs = 80) {
                Button(
                    onClick = onGame,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Játék", fontSize = 16.sp)
                }
            }

            AnimatedMenuItem(delayMs = 160) {
                Button(
                    onClick = onPuzzleEditor,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Szerkesztő", fontSize = 16.sp)
                }
            }

            AnimatedMenuItem(delayMs = 240) {
                Button(
                    onClick = onStatistics,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Statisztika", fontSize = 16.sp)
                }
            }

            AnimatedMenuItem(delayMs = 320) {
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Kilépés", fontSize = 16.sp)
                }
            }

            AnimatedMenuItem(delayMs = 400) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sötét mód",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedMenuItem(delayMs: Int, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        launch { alpha.animateTo(1f, animationSpec = tween(300)) }
        offsetY.animateTo(0f, animationSpec = tween(300))
    }

    Box(
        modifier = Modifier
            .alpha(alpha.value)
            .padding(top = offsetY.value.dp)
    ) {
        content()
    }
}
