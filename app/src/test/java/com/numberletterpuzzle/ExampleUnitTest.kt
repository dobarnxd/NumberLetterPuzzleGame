package com.numberletterpuzzle

import com.numberletterpuzzle.logic.PuzzleGenerator
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun puzzleGenerator_notNull() {
        val puzzle = PuzzleGenerator.generate("alma\nkörte")
        assertNotNull(puzzle)
        assertNotNull(puzzle.grid)
        assertNotNull(puzzle.solutionMapping)
    }
}
