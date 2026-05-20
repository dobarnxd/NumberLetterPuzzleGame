package com.numberletterpuzzle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameResultDao {

    @Insert
    suspend fun insert(result: GameResultEntity)

    @Query("SELECT * FROM game_results ORDER BY finishTimestamp DESC")
    suspend fun getAll(): List<GameResultEntity>

    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}
