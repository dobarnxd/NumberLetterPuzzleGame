package com.numberletterpuzzle.logic

import com.numberletterpuzzle.data.Cell
import com.numberletterpuzzle.data.Puzzle

object PuzzleGenerator {

    fun generate(boardString: String): Puzzle {
        val rows = boardString.lines()
        val filteredRows = mutableListOf<String>()
        for (row in rows) {
            if (row.isNotEmpty()) filteredRows.add(row)
        }

        // összegyűjtjük az összes egyedi betűt
        val allLetters = mutableSetOf<Char>()
        for (row in filteredRows) {
            for (ch in row) {
                if (ch != '#') {
                    allLetters.add(ch)
                }
            }
        }

        // véletlenszerű sorrendbe keverjük, majd számot rendelünk minden betűhöz
        val shuffledLetters = allLetters.toList().shuffled()
        val letterToNumber = mutableMapOf<Char, Int>()
        for (i in shuffledLetters.indices) {
            letterToNumber[shuffledLetters[i]] = i + 1
        }

        val numberToLetter = mutableMapOf<Int, Char>()
        for (entry in letterToNumber.entries) {
            numberToLetter[entry.value] = entry.key
        }

        // rácsko felépítése
        val grid = mutableListOf<List<Cell>>()
        for (rowIdx in filteredRows.indices) {
            val rowStr = filteredRows[rowIdx]
            val rowCells = mutableListOf<Cell>()
            for (colIdx in rowStr.indices) {
                val ch = rowStr[colIdx]
                val cell: Cell
                if (ch == '#') {
                    cell = Cell(row = rowIdx, col = colIdx, number = null, solutionLetter = null, isBlocked = true)
                } else {
                    cell = Cell(
                        row = rowIdx,
                        col = colIdx,
                        number = letterToNumber[ch],
                        solutionLetter = ch,
                        isBlocked = false
                    )
                }
                rowCells.add(cell)
            }
            grid.add(rowCells)
        }

        return Puzzle(
            grid = grid,
            solutionMapping = numberToLetter,
            letterMapping = letterToNumber
        )
    }
}
