package com.numberletterpuzzle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {

    @Query("SELECT * FROM puzzles WHERE isBuiltIn = 1 ORDER BY createdAt ASC")
    fun getBuiltInPuzzles(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE isBuiltIn = 0 ORDER BY createdAt DESC")
    fun getCustomPuzzles(): Flow<List<PuzzleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(puzzle: PuzzleEntity)

    @Query("DELETE FROM puzzles WHERE id = :id")
    suspend fun deleteById(id: String)
}
