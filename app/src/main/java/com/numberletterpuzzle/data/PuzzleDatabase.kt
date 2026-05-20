package com.numberletterpuzzle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PuzzleEntity::class, GameResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PuzzleDatabase : RoomDatabase() {

    abstract fun puzzleDao(): PuzzleDao
    abstract fun gameResultDao(): GameResultDao

    companion object {
        @Volatile private var INSTANCE: PuzzleDatabase? = null

        fun getDatabase(context: Context): PuzzleDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PuzzleDatabase::class.java,
                    "puzzle_database"
                )
                .addCallback(SeedCallback(context.applicationContext))
                .build()
                .also { INSTANCE = it }
            }

        private class SeedCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val puzzles = PuzzleData.loadPuzzles(context)
                val insert = db.compileStatement(
                    "INSERT OR IGNORE INTO puzzles (id, name, boardString, isBuiltIn, createdAt) " +
                    "VALUES (?, ?, ?, ?, ?)"
                )
                val now = System.currentTimeMillis()
                for (i in puzzles.indices) {
                    insert.clearBindings()
                    insert.bindString(1, "builtin_${i + 1}")
                    insert.bindString(2, puzzles[i].name)
                    insert.bindString(3, puzzles[i].boardString)
                    insert.bindLong(4, 1L)
                    insert.bindLong(5, now + i)
                    insert.executeInsert()
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                val puzzles = PuzzleData.loadPuzzles(context)
                val update = db.compileStatement(
                    "UPDATE puzzles SET name = ?, boardString = ? WHERE id = ?"
                )
                for (i in puzzles.indices) {
                    update.clearBindings()
                    update.bindString(1, puzzles[i].name)
                    update.bindString(2, puzzles[i].boardString)
                    update.bindString(3, "builtin_${i + 1}")
                    update.executeUpdateDelete()
                }
            }
        }
    }
}
