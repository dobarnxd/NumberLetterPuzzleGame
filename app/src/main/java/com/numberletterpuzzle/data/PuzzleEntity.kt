package com.numberletterpuzzle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val boardString: String,
    val isBuiltIn: Boolean,
    val createdAt: Long
)
