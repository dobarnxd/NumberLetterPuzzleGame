package com.numberletterpuzzle.data

data class GameResult(
    val puzzleName: String,
    val elapsedMillis: Long,
    val mistakeCount: Int,
    val hintCount: Int,
    val solutionRevealed: Boolean,
    val finishTimestamp: Long
)

data class PuzzleStats(
    val completedCount: Int = 0,
    val bestTimeMillis: Long = 0L,
    val totalTimeMillis: Long = 0L,
    val totalMistakes: Int = 0,
    val totalHints: Int = 0,
    val revealedCount: Int = 0,
    val lastPlayedPuzzle: String = "",
    val lastElapsedMillis: Long = 0L,
    val lastCompletionTimestamp: Long = 0L
) {
    val averageTimeMillis: Long
        get() = if (completedCount > 0) totalTimeMillis / completedCount else 0L
}
