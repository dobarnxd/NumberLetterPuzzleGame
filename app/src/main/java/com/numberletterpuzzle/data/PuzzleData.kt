package com.numberletterpuzzle.data

import android.content.Context
import org.json.JSONArray

data class PuzzleDefinition(
    val name: String,
    val boardString: String
)

private fun makeBoard(vararg rows: String): String {
    val maxLen = rows.maxOf { it.length }
    return rows.mapIndexed { i, row ->
        row.padEnd(maxLen, '#')
    }.joinToString("\n")
}

object PuzzleData {
    fun loadPuzzles(context: Context): List<PuzzleDefinition> {
        return try {
            val json = context.assets.open("puzzles.json").bufferedReader().readText()
            val arr  = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj     = arr.getJSONObject(i)
                val rowsArr = obj.getJSONArray("rows")
                val rows    = Array(rowsArr.length()) { rowsArr.getString(it) }
                PuzzleDefinition(
                    name        = obj.getString("name"),
                    boardString = makeBoard(*rows)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
