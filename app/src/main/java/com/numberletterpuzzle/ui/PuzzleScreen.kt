package com.numberletterpuzzle.ui

import android.app.Application
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numberletterpuzzle.data.PuzzleDefinition
import com.numberletterpuzzle.ui.components.AlphabetTracker
import com.numberletterpuzzle.ui.components.HungarianKeyboard
import com.numberletterpuzzle.ui.components.PuzzleGrid
import com.numberletterpuzzle.ui.components.ResultDialog
import com.numberletterpuzzle.ui.theme.LocalPuzzleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleScreen(
    puzzleDefinition: PuzzleDefinition,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val puzzleViewModel: PuzzleViewModel = viewModel(
        key = "puzzle_${puzzleDefinition.name}",
        factory = PuzzleViewModelFactory(application, puzzleDefinition.name, puzzleDefinition.boardString)
    )
    val state by puzzleViewModel.uiState.collectAsState()

    DisposableEffect(puzzleViewModel) {
        puzzleViewModel.resumeTimer()
        onDispose { puzzleViewModel.pauseTimer() }
    }

    var showSolutionConfirm by remember { mutableStateOf(false) }

    if (showSolutionConfirm) {
        SolutionConfirmDialog(
            onConfirm = { showSolutionConfirm = false; puzzleViewModel.revealSolution() },
            onDismiss = { showSolutionConfirm = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(puzzleDefinition.name, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Vissza")
                        }
                    },
                    actions = {
                        Text(
                            text = formatTime(state.elapsedSeconds),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (state.isSolved) SolvedBanner()

                SelectedNumberInfo(
                    selectedNumber = state.selectedNumber,
                    assignedLetter = state.selectedNumber?.let { state.userMapping[it] },
                    isPreFilled = state.selectedNumber != null && state.selectedNumber in state.preFilledNumbers
                )
                HorizontalDivider()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(LocalPuzzleColors.current.gridBackground)
                ) {
                    PuzzleGrid(
                        puzzle = state.puzzle,
                        userMapping = state.userMapping,
                        selectedNumber = state.selectedNumber,
                        preFilledNumbers = state.preFilledNumbers,
                        onCellClick = { puzzleViewModel.selectNumber(it) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
                HorizontalDivider()

                Box(
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AlphabetTracker(
                        userMapping = state.userMapping,
                        selectedNumber = state.selectedNumber,
                        preFilledNumbers = state.preFilledNumbers
                    )
                }
                HorizontalDivider()

                val keyboardEnabled = state.selectedNumber != null &&
                    state.selectedNumber !in state.preFilledNumbers &&
                    !state.isSolved && !state.solutionRevealed
                HungarianKeyboard(
                    usedLetters = state.userMapping.values.toSet(),
                    enabled = keyboardEnabled,
                    onLetterClick = { puzzleViewModel.assignLetter(it) },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
                HorizontalDivider()

                GameButtons(
                    state = state,
                    onHint = { puzzleViewModel.useHint() },
                    onRevealClick = { showSolutionConfirm = true },
                    onClear = { puzzleViewModel.clearSelected() },
                    onCheck = { puzzleViewModel.checkSolution() },
                    onReset = { puzzleViewModel.reset() }
                )
            }
        }

        ResultDialog(
            visible = state.showResultDialog,
            isSolved = state.isSolved,
            solutionRevealed = state.solutionRevealed,
            elapsedSeconds = state.elapsedSeconds,
            mistakeCount = state.mistakeCount,
            hintCount = state.hintCount,
            onBackToMenu = { puzzleViewModel.dismissResultDialog(); onBack() },
            onPlayAgain = { puzzleViewModel.reset() }
        )
    }
}

@Composable
private fun SolutionConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Megoldás mutatása") },
        text = { Text("Biztosan megmutatod a megoldást? Ez nem számít rendes befejezésnek.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) { Text("Megmutat") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégse") } }
    )
}

@Composable
private fun SolvedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF388E3C))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Megoldva! Gratulálok!",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun SelectedNumberInfo(selectedNumber: Int?, assignedLetter: Char?, isPreFilled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when {
                selectedNumber == null -> "Kattints egy cellára"
                isPreFilled -> "Előre kitöltve: #$selectedNumber → ${assignedLetter?.uppercase() ?: "?"}"
                assignedLetter != null -> "Kiválasztva: #$selectedNumber → ${assignedLetter.uppercase()}"
                else -> "Kiválasztva: #$selectedNumber"
            },
            fontSize = 13.sp,
            color = when {
                selectedNumber == null -> MaterialTheme.colorScheme.onSurfaceVariant
                isPreFilled -> LocalPuzzleColors.current.preFilledLetter
                else -> MaterialTheme.colorScheme.primary
            },
            fontWeight = if (selectedNumber != null) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun GameButtons(
    state: PuzzleUiState,
    onHint: () -> Unit,
    onRevealClick: () -> Unit,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onReset: () -> Unit
) {
    val gameOver = state.isSolved || state.solutionRevealed
    val canClear = !gameOver &&
        state.selectedNumber != null &&
        state.userMapping.containsKey(state.selectedNumber) &&
        state.selectedNumber !in state.preFilledNumbers

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onHint,
            modifier = Modifier.weight(1f),
            enabled = !gameOver && state.hintCount < PuzzleViewModel.MAX_HINTS
        ) {
            Text("Tipp  ${state.hintCount}/${PuzzleViewModel.MAX_HINTS}", fontSize = 12.sp)
        }
        Button(
            onClick = onRevealClick,
            modifier = Modifier.weight(1f),
            enabled = !gameOver,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78909C))
        ) {
            Text("Megoldás", fontSize = 11.sp)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f), enabled = canClear) {
            Text("Töröl", fontSize = 12.sp)
        }
        Button(onClick = onCheck, modifier = Modifier.weight(2f), enabled = !gameOver) {
            Text("Ellenőrzés", fontSize = 12.sp)
        }
        OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
            Text("Újrakezd", fontSize = 12.sp)
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
