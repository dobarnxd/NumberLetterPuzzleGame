package com.numberletterpuzzle.data

data class Cell(
    val row: Int,
    val col: Int,
    val number: Int?,
    val solutionLetter: Char?,
    val isBlocked: Boolean
)
