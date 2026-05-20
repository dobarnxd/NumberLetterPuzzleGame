package com.numberletterpuzzle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val puzzleName: String,
    val elapsedMillis: Long,
    val mistakeCount: Int,
    val hintCount: Int,
    val solutionRevealed: Boolean,
    val finishTimestamp: Long
)
