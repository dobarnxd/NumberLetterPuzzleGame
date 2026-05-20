package com.numberletterpuzzle.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numberletterpuzzle.data.Cell
import com.numberletterpuzzle.ui.theme.LocalPuzzleColors

@Composable
fun GridCell(
    cell: Cell,
    cellSize: Dp,
    userLetter: Char?,
    isSelected: Boolean,
    isPreFilled: Boolean = false,
    isWrong: Boolean = false
) {
    val colors = LocalPuzzleColors.current
    val numberFontSize = (cellSize.value * 0.20f).coerceIn(6f, 12f).sp
    val letterFontSize = (cellSize.value * 0.42f).coerceIn(12f, 24f).sp
    val numberLineHeight = (numberFontSize.value + 1f).sp
    val letterLineHeight = (letterFontSize.value + 1f).sp

    if (cell.isBlocked) {
        Box(
            modifier = Modifier
                .size(cellSize)
                .background(colors.blockedCell)
                .border(0.5.dp, colors.blockedCellBorder)
        )
    } else {
        val targetBg = when {
            isSelected  -> colors.selectedCell
            isWrong     -> colors.wrongCell
            isPreFilled -> colors.preFilledCell
            else        -> colors.cellBackground
        }
        val targetBorder = if (isSelected) colors.selectedBorder else colors.cellBorder

        val bgColor by animateColorAsState(
            targetValue = targetBg,
            animationSpec = tween(200),
            label = "cellBg"
        )
        val borderColor by animateColorAsState(
            targetValue = targetBorder,
            animationSpec = tween(200),
            label = "cellBorder"
        )

        val letterColor = if (isPreFilled) colors.preFilledLetter else colors.letterColor
        val letterFontStyle = if (isPreFilled) FontStyle.Italic else FontStyle.Normal

        Box(
            modifier = Modifier
                .size(cellSize)
                .background(bgColor)
                .border(0.5.dp, borderColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cell.number?.toString() ?: "",
                    fontSize = numberFontSize,
                    color = colors.numberColor,
                    lineHeight = numberLineHeight,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                AnimatedContent(
                    targetState = userLetter?.uppercase() ?: "",
                    transitionSpec = {
                        (fadeIn(tween(150)) + scaleIn(initialScale = 0.6f, animationSpec = tween(150))) togetherWith
                            fadeOut(tween(100))
                    },
                    label = "cellLetter"
                ) { letterText ->
                    Text(
                        text = letterText,
                        fontSize = letterFontSize,
                        fontWeight = FontWeight.Bold,
                        fontStyle = letterFontStyle,
                        color = letterColor,
                        lineHeight = letterLineHeight,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
