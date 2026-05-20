package com.numberletterpuzzle.data

data class Puzzle(
    val grid: List<List<Cell>>,
    val solutionMapping: Map<Int, Char>,
    val letterMapping: Map<Char, Int>
)
