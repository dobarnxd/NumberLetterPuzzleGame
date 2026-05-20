package com.numberletterpuzzle.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PuzzleColors(
    val cellBackground: Color,
    val cellBorder: Color,
    val blockedCell: Color,
    val blockedCellBorder: Color,
    val selectedCell: Color,
    val selectedBorder: Color,
    val preFilledCell: Color,
    val wrongCell: Color,
    val gridBackground: Color,
    val letterColor: Color,
    val preFilledLetter: Color,
    val numberColor: Color
)

val LightPuzzleColors = PuzzleColors(
    cellBackground    = Color.White,
    cellBorder        = Color(0xFFBDBDBD),
    blockedCell       = Color(0xFF2C2C2C),
    blockedCellBorder = Color(0xFF1A1A1A),
    selectedCell      = Color(0xFFBBDEFB),
    selectedBorder    = Color(0xFF1565C0),
    preFilledCell     = Color(0xFFFFF9C4),
    wrongCell         = Color(0xFFFFCDD2),
    gridBackground    = Color(0xFFEEEEEE),
    letterColor       = Color(0xFF212121),
    preFilledLetter   = Color(0xFF5D4037),
    numberColor       = Color(0xFF9E9E9E)
)

val DarkPuzzleColors = PuzzleColors(
    cellBackground    = Color(0xFF2D2D2D),
    cellBorder        = Color(0xFF505050),
    blockedCell       = Color(0xFF111111),
    blockedCellBorder = Color(0xFF000000),
    selectedCell      = Color(0xFF1A3A6B),
    selectedBorder    = Color(0xFF64B5F6),
    preFilledCell     = Color(0xFF3D3000),
    wrongCell         = Color(0xFF6B1A1A),
    gridBackground    = Color(0xFF1A1A1A),
    letterColor       = Color(0xFFE8E8E8),
    preFilledLetter   = Color(0xFFFFB74D),
    numberColor       = Color(0xFF808080)
)

val LocalPuzzleColors = staticCompositionLocalOf { LightPuzzleColors }
