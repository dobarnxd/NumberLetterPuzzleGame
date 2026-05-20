package com.numberletterpuzzle.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleGeneratorTest {

    @Test
    fun generate_rowCountIsCorrect() {
        val board = "abc\ndef\nghi"
        val puzzle = PuzzleGenerator.generate(board)
        assertEquals(3, puzzle.grid.size)
    }

    @Test
    fun generate_columnCountIsCorrect() {
        val board = "abcd\nefgh"
        val puzzle = PuzzleGenerator.generate(board)
        assertEquals(4, puzzle.grid[0].size)
    }

    @Test
    fun generate_blockedCellsAreCorrect() {
        val board = "a#b\n#cd"
        val puzzle = PuzzleGenerator.generate(board)
        assertFalse(puzzle.grid[0][0].isBlocked)
        assertTrue(puzzle.grid[0][1].isBlocked)
        assertFalse(puzzle.grid[0][2].isBlocked)
        assertTrue(puzzle.grid[1][0].isBlocked)
    }

    @Test
    fun generate_blockedCellsHaveNullNumberAndLetter() {
        val board = "a#b"
        val puzzle = PuzzleGenerator.generate(board)
        assertNull(puzzle.grid[0][1].number)
        assertNull(puzzle.grid[0][1].solutionLetter)
    }

    @Test
    fun generate_activeCellsHaveNumberAndLetter() {
        val board = "abc"
        val puzzle = PuzzleGenerator.generate(board)
        for (cell in puzzle.grid[0]) {
            assertNotNull(cell.number)
            assertNotNull(cell.solutionLetter)
            assertFalse(cell.isBlocked)
        }
    }

    @Test
    fun generate_uniqueLettersGetUniqueNumbers() {
        val board = "abcdef"
        val puzzle = PuzzleGenerator.generate(board)
        val numbers = puzzle.letterMapping.values.toList()
        assertEquals(numbers.size, numbers.distinct().size)
    }

    @Test
    fun generate_solutionMappingAndLetterMappingAreInverse() {
        val board = "alma\nkörte"
        val puzzle = PuzzleGenerator.generate(board)
        for ((letter, number) in puzzle.letterMapping) {
            assertEquals(letter, puzzle.solutionMapping[number])
        }
    }

    @Test
    fun generate_emptyLinesIgnored() {
        val board = "abc\n\ndef"
        val puzzle = PuzzleGenerator.generate(board)
        assertEquals(2, puzzle.grid.size)
    }

    @Test
    fun generate_repeatedLetterGetsSameNumber() {
        val board = "aba"
        val puzzle = PuzzleGenerator.generate(board)
        val num1 = puzzle.grid[0][0].number
        val num2 = puzzle.grid[0][2].number
        assertEquals(num1, num2)
    }

    @Test
    fun generate_duplicateLetterAppearsOnceInSolutionMapping() {
        val board = "aab"
        val puzzle = PuzzleGenerator.generate(board)
        assertEquals(2, puzzle.solutionMapping.size)
    }

    @Test
    fun generate_cellIndicesAreCorrect() {
        val board = "ab\ncd"
        val puzzle = PuzzleGenerator.generate(board)
        assertEquals(0, puzzle.grid[0][0].row)
        assertEquals(0, puzzle.grid[0][0].col)
        assertEquals(1, puzzle.grid[1][0].row)
        assertEquals(0, puzzle.grid[1][0].col)
        assertEquals(1, puzzle.grid[1][1].col)
    }

    @Test
    fun generate_numbersStartFromOne() {
        val board = "abc"
        val puzzle = PuzzleGenerator.generate(board)
        val numbers = puzzle.solutionMapping.keys.sorted()
        assertEquals(1, numbers.first())
        assertEquals(3, numbers.last())
    }

    @Test
    fun generate_fullGrid_correctSize() {
        val board = "a#b\nc#d\ne#f"
        val puzzle = PuzzleGenerator.generate(board)
        assertEquals(3, puzzle.grid.size)
        for (row in puzzle.grid) {
            assertEquals(3, row.size)
        }
    }

    @Test
    fun generate_emptyBoard_emptyGrid() {
        val puzzle = PuzzleGenerator.generate("")
        assertTrue(puzzle.grid.isEmpty())
        assertTrue(puzzle.solutionMapping.isEmpty())
        assertTrue(puzzle.letterMapping.isEmpty())
    }

    @Test
    fun generate_onlyBlockedCells_emptyMappings() {
        val puzzle = PuzzleGenerator.generate("###")
        assertEquals(1, puzzle.grid.size)
        assertEquals(3, puzzle.grid[0].size)
        assertTrue(puzzle.solutionMapping.isEmpty())
        assertTrue(puzzle.letterMapping.isEmpty())
    }

    @Test
    fun generate_singleCell_getsNumberOne() {
        val puzzle = PuzzleGenerator.generate("a")
        assertEquals(1, puzzle.grid[0][0].number)
        assertEquals('a', puzzle.grid[0][0].solutionLetter)
        assertFalse(puzzle.grid[0][0].isBlocked)
    }

    @Test
    fun generate_allSameLetter_singleEntryInMapping() {
        val puzzle = PuzzleGenerator.generate("aaa")
        assertEquals(1, puzzle.letterMapping.size)
        assertEquals(1, puzzle.solutionMapping.size)
    }
}
