package com.numberletterpuzzle.ui

import com.numberletterpuzzle.data.PuzzleDefinition

sealed class Screen {
    object MainMenu : Screen()
    object PuzzleSelection : Screen()
    data class Game(val puzzleDefinition: PuzzleDefinition) : Screen()
    object PuzzleEditor : Screen()
    object Statistics : Screen()
}
