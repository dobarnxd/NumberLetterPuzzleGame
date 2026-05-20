package com.numberletterpuzzle.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsRepositoryTest {

    private fun makeResult(
        name: String = "Teszt rejtvény",
        elapsedMs: Long = 60_000L,
        mistakes: Int = 0,
        hints: Int = 0,
        revealed: Boolean = false,
        finishMs: Long = System.currentTimeMillis()
    ): GameResultEntity {
        return GameResultEntity(
            puzzleName = name,
            elapsedMillis = elapsedMs,
            mistakeCount = mistakes,
            hintCount = hints,
            solutionRevealed = revealed,
            finishTimestamp = finishMs
        )
    }

    private fun makeRepository(results: List<GameResultEntity>): StatisticsRepository {
        val fakeDao = object : GameResultDao {
            private val list = results.sortedByDescending { it.finishTimestamp }
            override suspend fun insert(result: GameResultEntity) {}
            override suspend fun getAll(): List<GameResultEntity> = list
            override suspend fun deleteAll() {}
        }
        return StatisticsRepository(fakeDao)
    }

    @Test
    fun getStats_emptyList_returnsDefaultStats() = runBlocking {
        val repo = makeRepository(emptyList())
        val stats = repo.getStats()
        assertEquals(0, stats.completedCount)
        assertEquals(0L, stats.bestTimeMillis)
        assertEquals(0, stats.totalMistakes)
        assertEquals(0, stats.revealedCount)
    }

    @Test
    fun getStats_oneSolvedGame_correctStats() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(name = "Animals", elapsedMs = 90_000L, mistakes = 2, hints = 1)
        ))
        val stats = repo.getStats()
        assertEquals(1, stats.completedCount)
        assertEquals(90_000L, stats.bestTimeMillis)
        assertEquals(90_000L, stats.totalTimeMillis)
        assertEquals(2, stats.totalMistakes)
        assertEquals(1, stats.totalHints)
        assertEquals(0, stats.revealedCount)
    }

    @Test
    fun getStats_multipleSolvedGames_bestTime() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(elapsedMs = 120_000L),
            makeResult(elapsedMs = 45_000L),
            makeResult(elapsedMs = 90_000L)
        ))
        val stats = repo.getStats()
        assertEquals(3, stats.completedCount)
        assertEquals(45_000L, stats.bestTimeMillis)
    }

    @Test
    fun getStats_revealedSolution_notCountedAsCompleted() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(revealed = true)
        ))
        val stats = repo.getStats()
        assertEquals(0, stats.completedCount)
        assertEquals(1, stats.revealedCount)
        assertEquals(0L, stats.bestTimeMillis)
    }

    @Test
    fun getStats_mixed_correctCategories() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(elapsedMs = 60_000L, revealed = false),
            makeResult(elapsedMs = 80_000L, revealed = true),
            makeResult(elapsedMs = 40_000L, revealed = false)
        ))
        val stats = repo.getStats()
        assertEquals(2, stats.completedCount)
        assertEquals(1, stats.revealedCount)
        assertEquals(40_000L, stats.bestTimeMillis)
        assertEquals(100_000L, stats.totalTimeMillis)
    }

    @Test
    fun getStats_averageTime_isCorrect() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(elapsedMs = 60_000L),
            makeResult(elapsedMs = 120_000L)
        ))
        val stats = repo.getStats()
        assertEquals(90_000L, stats.averageTimeMillis)
    }

    @Test
    fun getStats_averageTime_noCompletedGames_isZero() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(revealed = true)
        ))
        val stats = repo.getStats()
        assertEquals(0L, stats.averageTimeMillis)
    }

    @Test
    fun getStats_lastPlayedPuzzle_isCorrect() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(name = "Latest puzzle", finishMs = 2000L),
            makeResult(name = "Earlier puzzle", finishMs = 1000L)
        ))
        val stats = repo.getStats()
        assertEquals("Latest puzzle", stats.lastPlayedPuzzle)
    }

    @Test
    fun getStats_totalHints_addsCorrectly() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(hints = 2),
            makeResult(hints = 1),
            makeResult(hints = 3)
        ))
        val stats = repo.getStats()
        assertEquals(6, stats.totalHints)
    }

    @Test
    fun getStats_totalMistakes_addsCorrectly() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(mistakes = 3),
            makeResult(mistakes = 5, revealed = true)
        ))
        val stats = repo.getStats()
        assertEquals(8, stats.totalMistakes)
    }

    @Test
    fun getStats_singleRevealedGame_lastElapsedTimeCorrect() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(elapsedMs = 55_000L, revealed = true)
        ))
        val stats = repo.getStats()
        assertEquals(55_000L, stats.lastElapsedMillis)
        assertTrue(stats.completedCount == 0)
        assertTrue(stats.revealedCount == 1)
    }

    @Test
    fun getStats_allRevealed_bestTimeRemainsZero() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(elapsedMs = 30_000L, revealed = true),
            makeResult(elapsedMs = 60_000L, revealed = true)
        ))
        val stats = repo.getStats()
        assertEquals(0, stats.completedCount)
        assertEquals(0L, stats.bestTimeMillis)
        assertEquals(2, stats.revealedCount)
    }

    @Test
    fun getStats_lastCompletionTimestamp_isCorrect() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(name = "First", finishMs = 5000L),
            makeResult(name = "Second", finishMs = 1000L)
        ))
        val stats = repo.getStats()
        assertEquals(5000L, stats.lastCompletionTimestamp)
    }

    @Test
    fun getStats_lastGameIsRevealed_stillTrackedAsLastPlayed() = runBlocking {
        val repo = makeRepository(listOf(
            makeResult(name = "Revealed puzzle", finishMs = 3000L, revealed = true),
            makeResult(name = "Completed puzzle", finishMs = 1000L, revealed = false)
        ))
        val stats = repo.getStats()
        assertEquals("Revealed puzzle", stats.lastPlayedPuzzle)
        assertEquals(1, stats.completedCount)
        assertEquals(1, stats.revealedCount)
    }
}
