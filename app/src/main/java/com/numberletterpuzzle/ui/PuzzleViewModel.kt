package com.numberletterpuzzle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numberletterpuzzle.data.GameResult
import com.numberletterpuzzle.data.Puzzle
import com.numberletterpuzzle.data.PuzzleDatabase
import com.numberletterpuzzle.data.StatisticsRepository
import com.numberletterpuzzle.logic.PuzzleGenerator
import com.numberletterpuzzle.logic.PuzzleValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PuzzleUiState(
    val puzzle: Puzzle,
    val userMapping: Map<Int, Char> = emptyMap(),
    val selectedNumber: Int? = null,
    val isSolved: Boolean = false,
    val preFilledNumbers: Set<Int> = emptySet(),
    val elapsedSeconds: Long = 0L,
    val mistakeCount: Int = 0,
    val hintCount: Int = 0,
    val solutionRevealed: Boolean = false,
    val showResultDialog: Boolean = false,
    val wrongNumbers: Set<Int> = emptySet()
)

class PuzzleViewModel(
    application: Application,
    private val puzzleName: String,
    private val boardString: String
) : AndroidViewModel(application) {

    private val repository = StatisticsRepository(PuzzleDatabase.getDatabase(application).gameResultDao())

    private val initialPuzzle: Puzzle = PuzzleGenerator.generate(boardString)
    private val preFilledNumbers: Set<Int> = pickPreFilledNumbers(initialPuzzle, 3)
    private val initialPreFilledMapping: Map<Int, Char> = buildPreFilledMapping(initialPuzzle, preFilledNumbers)

    private val countedMistakes = mutableSetOf<Pair<Int, Char>>()
    private var timerJob: Job? = null
    private var finishTimeMillis: Long = 0L

    private val _uiState = MutableStateFlow(
        PuzzleUiState(
            puzzle = initialPuzzle,
            userMapping = initialPreFilledMapping,
            preFilledNumbers = preFilledNumbers
        )
    )
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    init {
        startTimer()
    }

    fun selectNumber(number: Int) {
        _uiState.update { it.copy(selectedNumber = number) }
    }

    fun assignLetter(letter: Char) {
        val state = _uiState.value
        val number = state.selectedNumber ?: return
        if (number in state.preFilledNumbers) return
        if (state.isSolved || state.solutionRevealed) return

        val lower = letter.lowercaseChar()
        val solutionLetter = state.puzzle.solutionMapping[number]
        val isWrong = solutionLetter != null && solutionLetter != lower

        _uiState.update { s ->
            val map = s.userMapping.toMutableMap()
            val prevNumber = map.entries.find { it.value == lower }?.key
            if (prevNumber != null && prevNumber != number) {
                if (prevNumber in s.preFilledNumbers) return@update s
                map.remove(prevNumber)
            }
            map[number] = lower
            val addedMistake = if (isWrong && countedMistakes.add(number to lower)) 1 else 0
            s.copy(
                userMapping = map,
                isSolved = false,
                mistakeCount = s.mistakeCount + addedMistake,
                wrongNumbers = s.wrongNumbers - number
            )
        }
    }

    fun clearSelected() {
        val state = _uiState.value
        val number = state.selectedNumber ?: return
        if (number in state.preFilledNumbers) return
        if (state.isSolved || state.solutionRevealed) return
        _uiState.update { s ->
            val map = s.userMapping.toMutableMap()
            map.remove(number)
            s.copy(userMapping = map, isSolved = false, wrongNumbers = s.wrongNumbers - number)
        }
    }

    fun checkSolution() {
        val state = _uiState.value
        if (state.isSolved || state.solutionRevealed) return

        val result = PuzzleValidator.validate(state.puzzle, state.userMapping)

        var addedMistakes = 0
        for ((row, col) in result.incorrectCells) {
            val cell = state.puzzle.grid[row][col]
            val number = cell.number ?: continue
            val assigned = state.userMapping[number] ?: continue
            if (countedMistakes.add(number to assigned)) addedMistakes++
        }

        if (result.isSolved) {
            stopTimer()
            finishTimeMillis = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    isSolved = true,
                    mistakeCount = it.mistakeCount + addedMistakes,
                    showResultDialog = true,
                    wrongNumbers = emptySet()
                )
            }
            saveGameResult(revealed = false)
        } else {
            val wrongNums = result.incorrectCells.mapNotNull { (row, col) ->
                val cell = state.puzzle.grid[row][col]
                val num = cell.number ?: return@mapNotNull null
                if (state.userMapping.containsKey(num)) num else null
            }.toSet()
            _uiState.update {
                it.copy(mistakeCount = it.mistakeCount + addedMistakes, wrongNumbers = wrongNums)
            }
        }
    }

    fun useHint() {
        val state = _uiState.value
        if (state.isSolved || state.solutionRevealed) return
        if (state.hintCount >= MAX_HINTS) return

        val availableNumbers = state.puzzle.solutionMapping.keys.filter { num ->
            num !in state.preFilledNumbers &&
                state.userMapping[num] != state.puzzle.solutionMapping[num]
        }
        val hintNumber = availableNumbers.randomOrNull() ?: return
        val hintLetter = state.puzzle.solutionMapping[hintNumber] ?: return

        _uiState.update { s ->
            val map = s.userMapping.toMutableMap()
            val prevNumber = map.entries.find { it.value == hintLetter }?.key
            if (prevNumber != null && prevNumber != hintNumber && prevNumber !in s.preFilledNumbers) {
                map.remove(prevNumber)
            }
            map[hintNumber] = hintLetter
            s.copy(
                userMapping = map,
                preFilledNumbers = s.preFilledNumbers + hintNumber,
                hintCount = s.hintCount + 1
            )
        }
    }

    fun revealSolution() {
        val state = _uiState.value
        if (state.isSolved || state.solutionRevealed) return

        stopTimer()
        finishTimeMillis = System.currentTimeMillis()
        val allNumbers = state.puzzle.solutionMapping.keys.toSet()

        _uiState.update {
            it.copy(
                userMapping = state.puzzle.solutionMapping.toMap(),
                preFilledNumbers = allNumbers,
                solutionRevealed = true,
                showResultDialog = true
            )
        }
        saveGameResult(revealed = true)
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeTimer() {
        val state = _uiState.value
        if (!state.isSolved && !state.solutionRevealed) {
            startTimer()
        }
    }

    fun reset() {
        countedMistakes.clear()
        stopTimer()
        finishTimeMillis = 0L
        val newPuzzle       = PuzzleGenerator.generate(boardString)
        val newPreFilled    = pickPreFilledNumbers(newPuzzle, 3)
        val newPreFilledMap = buildPreFilledMapping(newPuzzle, newPreFilled)
        _uiState.value = PuzzleUiState(
            puzzle           = newPuzzle,
            userMapping      = newPreFilledMap,
            preFilledNumbers = newPreFilled,
            elapsedSeconds   = 0L,
            wrongNumbers     = emptySet()
        )
        startTimer()
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveGameResult(revealed: Boolean) {
        val state = _uiState.value
        viewModelScope.launch {
            repository.saveGameResult(
                GameResult(
                    puzzleName       = puzzleName,
                    elapsedMillis    = state.elapsedSeconds * 1_000L,
                    mistakeCount     = state.mistakeCount,
                    hintCount        = state.hintCount,
                    solutionRevealed = revealed,
                    finishTimestamp  = finishTimeMillis
                )
            )
        }
    }

    companion object {
        const val MAX_HINTS = 3

        private fun pickPreFilledNumbers(puzzle: Puzzle, count: Int): Set<Int> {
            val allNumbers = puzzle.solutionMapping.keys.toList().shuffled()
            val result = mutableSetOf<Int>()
            for (i in 0 until minOf(count, allNumbers.size)) {
                result.add(allNumbers[i])
            }
            return result
        }

        private fun buildPreFilledMapping(puzzle: Puzzle, numbers: Set<Int>): Map<Int, Char> {
            val result = mutableMapOf<Int, Char>()
            for (num in numbers) {
                val letter = puzzle.solutionMapping[num]
                if (letter != null) {
                    result[num] = letter
                }
            }
            return result
        }
    }
}

class PuzzleViewModelFactory(
    private val application: Application,
    private val puzzleName: String,
    private val boardString: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PuzzleViewModel(application, puzzleName, boardString) as T
    }
}
