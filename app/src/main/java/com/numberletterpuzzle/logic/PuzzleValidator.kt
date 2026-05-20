package com.numberletterpuzzle.logic

import com.numberletterpuzzle.data.Puzzle

data class ValidationResult(
    val incorrectCells: Set<Pair<Int, Int>>,
    val isSolved: Boolean
)

object PuzzleValidator {

    fun validate(puzzle: Puzzle, userMapping: Map<Int, Char>): ValidationResult {
        val incorrectCells = mutableSetOf<Pair<Int, Int>>()

        for (row in puzzle.grid) {
            for (cell in row) {
                if (cell.isBlocked) continue
                val number = cell.number ?: continue
                val userLetter = userMapping[number]
                if (userLetter == null || userLetter != cell.solutionLetter) {
                    incorrectCells.add(Pair(cell.row, cell.col))
                }
            }
        }

        return ValidationResult(
            incorrectCells = incorrectCells,
            isSolved = incorrectCells.isEmpty()
        )
    }
}
