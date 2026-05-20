package com.numberletterpuzzle.data

class StatisticsRepository(private val dao: GameResultDao) {

    suspend fun getStats(): PuzzleStats {
        val results = dao.getAll()
        if (results.isEmpty()) return PuzzleStats()

        var completedCount = 0
        var bestTime = 0L
        var totalTime = 0L
        var totalMistakes = 0
        var totalHints = 0
        var revealedCount = 0

        for (result in results) {
            totalMistakes += result.mistakeCount
            totalHints += result.hintCount
            if (result.solutionRevealed) {
                revealedCount++
            } else {
                completedCount++
                totalTime += result.elapsedMillis
                if (bestTime == 0L || result.elapsedMillis < bestTime) {
                    bestTime = result.elapsedMillis
                }
            }
        }

        val last = results.first()
        return PuzzleStats(
            completedCount          = completedCount,
            bestTimeMillis          = bestTime,
            totalTimeMillis         = totalTime,
            totalMistakes           = totalMistakes,
            totalHints              = totalHints,
            revealedCount           = revealedCount,
            lastPlayedPuzzle        = last.puzzleName,
            lastElapsedMillis       = last.elapsedMillis,
            lastCompletionTimestamp = last.finishTimestamp
        )
    }

    suspend fun saveGameResult(result: GameResult) {
        dao.insert(
            GameResultEntity(
                puzzleName       = result.puzzleName,
                elapsedMillis    = result.elapsedMillis,
                mistakeCount     = result.mistakeCount,
                hintCount        = result.hintCount,
                solutionRevealed = result.solutionRevealed,
                finishTimestamp  = result.finishTimestamp
            )
        )
    }

    suspend fun clearStats() {
        dao.deleteAll()
    }
}
