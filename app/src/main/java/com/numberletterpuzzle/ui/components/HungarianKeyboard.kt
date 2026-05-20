package com.numberletterpuzzle.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
val HUNGARIAN_KEYBOARD_ROWS: List<List<Char>> = listOf(
    listOf('a', 'á', 'b', 'c', 'd', 'e', 'é', 'f', 'g', 'h', 'i', 'í'),
    listOf('j', 'k', 'l', 'm', 'n', 'o', 'ó', 'ö', 'ő', 'p', 'q', 'r'),
    listOf('s', 't', 'u', 'ú', 'ü', 'ű', 'v', 'w', 'x', 'y', 'z')
)

@Composable
fun HungarianKeyboard(
    usedLetters: Set<Char>,
    enabled: Boolean,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in HUNGARIAN_KEYBOARD_ROWS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (letter in row) {
                    LetterKey(
                        letter = letter,
                        isUsed = letter in usedLetters,
                        onClick = { if (enabled) onLetterClick(letter) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterKey(
    letter: Char,
    isUsed: Boolean,
    onClick: () -> Unit
) {
    val targetBg   = if (isUsed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val targetText = if (isUsed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val bgColor   by animateColorAsState(targetValue = targetBg,   animationSpec = tween(200), label = "keyBg")
    val textColor by animateColorAsState(targetValue = targetText, animationSpec = tween(200), label = "keyText")

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(28.dp)
            .height(34.dp),
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        tonalElevation = if (isUsed) 0.dp else 2.dp,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter.uppercase(),
                fontSize = 11.sp,
                fontWeight = if (isUsed) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}
