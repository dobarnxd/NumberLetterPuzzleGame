package com.numberletterpuzzle.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultDialog(
    visible: Boolean,
    isSolved: Boolean,
    solutionRevealed: Boolean,
    elapsedSeconds: Long,
    mistakeCount: Int,
    hintCount: Int,
    onBackToMenu: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val m = elapsedSeconds / 60
    val s = elapsedSeconds % 60
    val timeStr = "%02d:%02d".format(m, s)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit  = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.82f, animationSpec = tween(300)) + fadeIn(tween(300)),
                exit  = scaleOut(targetScale = 0.82f, animationSpec = tween(200)) + fadeOut(tween(200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (solutionRevealed) "Megoldás felfedve" else "Gratulálok!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        StatRow(label = "Idő", value = timeStr)
                        StatRow(label = "Hibák", value = "$mistakeCount")
                        StatRow(label = "Tippek", value = "$hintCount")
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onBackToMenu,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Vissza")
                            }
                            Button(
                                onClick = onPlayAgain,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Újra")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
