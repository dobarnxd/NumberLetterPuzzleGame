package com.numberletterpuzzle.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numberletterpuzzle.ui.theme.LocalPuzzleColors

@Composable
fun AlphabetTracker(
    userMapping: Map<Int, Char>,
    selectedNumber: Int?,
    modifier: Modifier = Modifier,
    preFilledNumbers: Set<Int> = emptySet()
) {
    val puzzleColors = LocalPuzzleColors.current

    if (userMapping.isEmpty()) {
        Text(
            text = "Kattints egy cellára, majd válassz betűt",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(horizontal = 12.dp)
        )
        return
    }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for ((number, letter) in userMapping.toSortedMap()) {
            val isHighlighted = number == selectedNumber
            val isPreFilled   = number in preFilledNumbers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 9.sp,
                    color = when {
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        isPreFilled   -> puzzleColors.preFilledLetter
                        else          -> puzzleColors.numberColor
                    },
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = letter.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = if (isHighlighted || isPreFilled) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        isPreFilled   -> puzzleColors.preFilledLetter
                        else          -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
