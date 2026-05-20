package com.numberletterpuzzle.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomPuzzleRepository(private val dao: PuzzleDao) {

    fun getAll(): Flow<List<CustomPuzzle>> {
        return dao.getCustomPuzzles().map { entityList ->
            val result = mutableListOf<CustomPuzzle>()
            for (entity in entityList) {
                result.add(CustomPuzzle(entity.id, entity.name, entity.boardString, entity.createdAt))
            }
            result
        }
    }

    suspend fun save(puzzle: CustomPuzzle) {
        dao.insert(
            PuzzleEntity(
                id = puzzle.id,
                name = puzzle.name,
                boardString = puzzle.boardString,
                isBuiltIn = false,
                createdAt = puzzle.createdTimestamp
            )
        )
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
