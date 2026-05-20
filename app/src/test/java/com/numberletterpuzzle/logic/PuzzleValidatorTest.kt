package com.numberletterpuzzle.logic

import com.numberletterpuzzle.data.Cell
import com.numberletterpuzzle.data.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleValidatorTest {

    private fun buildPuzzle(
        rows: List<List<Triple<Int?, Char?, Boolean>>>
    ): Puzzle {
        val grid = mutableListOf<List<Cell>>()
        val solutionMap = mutableMapOf<Int, Char>()
        val letterMap = mutableMapOf<Char, Int>()

        for (rowIdx in rows.indices) {
            val row = mutableListOf<Cell>()
            for (colIdx in rows[rowIdx].indices) {
                val (number, letter, blocked) = rows[rowIdx][colIdx]
                if (number != null && letter != null) {
                    solutionMap[number] = letter
                    letterMap[letter] = number
                }
                row.add(Cell(rowIdx, colIdx, number, letter, blocked))
            }
            grid.add(row)
        }
        return Puzzle(grid, solutionMap, letterMap)
    }

    @Test
    fun validate_correctSolution_markedAsSolved() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(2, 'b', false), Triple(3, 'c', false))
        ))
        val userMapping = mapOf(1 to 'a', 2 to 'b', 3 to 'c')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertTrue(result.isSolved)
        assertTrue(result.incorrectCells.isEmpty())
    }

    @Test
    fun validate_wrongLetter_notSolved() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(2, 'b', false))
        ))
        val userMapping = mapOf(1 to 'a', 2 to 'x')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertFalse(result.isSolved)
        assertTrue(result.incorrectCells.isNotEmpty())
    }

    @Test
    fun validate_missingLetter_notSolved() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(2, 'b', false))
        ))
        val userMapping = mapOf(1 to 'a')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertFalse(result.isSolved)
    }

    @Test
    fun validate_emptyMapping_notSolved() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(2, 'b', false))
        ))
        val result = PuzzleValidator.validate(puzzle, emptyMap())
        assertFalse(result.isSolved)
        assertEquals(2, result.incorrectCells.size)
    }

    @Test
    fun validate_blockedCellsDoNotCount() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(null, null, true), Triple(2, 'b', false))
        ))
        val userMapping = mapOf(1 to 'a', 2 to 'b')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertTrue(result.isSolved)
    }

    @Test
    fun validate_incorrectCellCoordinates_areCorrect() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(2, 'b', false), Triple(3, 'c', false))
        ))
        val userMapping = mapOf(1 to 'a', 2 to 'x', 3 to 'c')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertTrue(result.incorrectCells.contains(Pair(0, 1)))
        assertFalse(result.incorrectCells.contains(Pair(0, 0)))
        assertFalse(result.incorrectCells.contains(Pair(0, 2)))
    }

    @Test
    fun validate_multiRow_correctCellReference() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false), Triple(2, 'b', false)),
            listOf(Triple(3, 'c', false), Triple(4, 'd', false))
        ))
        val userMapping = mapOf(1 to 'a', 2 to 'b', 3 to 'x', 4 to 'd')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertFalse(result.isSolved)
        assertTrue(result.incorrectCells.contains(Pair(1, 0)))
    }

    @Test
    fun validate_allBlocked_markedAsSolved() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(null, null, true), Triple(null, null, true))
        ))
        val result = PuzzleValidator.validate(puzzle, emptyMap())
        assertTrue(result.isSolved)
        assertTrue(result.incorrectCells.isEmpty())
    }

    @Test
    fun validate_uppercase_doesNotMatch() {
        val puzzle = buildPuzzle(listOf(
            listOf(Triple(1, 'a', false))
        ))
        val userMapping = mapOf(1 to 'A')
        val result = PuzzleValidator.validate(puzzle, userMapping)
        assertFalse(result.isSolved)
    }
}
