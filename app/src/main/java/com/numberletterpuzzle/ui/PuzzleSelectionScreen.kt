package com.numberletterpuzzle.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numberletterpuzzle.data.CustomPuzzle
import com.numberletterpuzzle.data.CustomPuzzleRepository
import com.numberletterpuzzle.data.PuzzleDatabase
import com.numberletterpuzzle.data.PuzzleDefinition
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleSelectionScreen(
    onPuzzleSelected: (PuzzleDefinition) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { PuzzleDatabase.getDatabase(context) }
    val repo = remember { CustomPuzzleRepository(db.puzzleDao()) }

    val builtInPuzzles by db.puzzleDao().getBuiltInPuzzles().collectAsState(initial = emptyList())
    val customPuzzles by repo.getAll().collectAsState(initial = emptyList())

    var puzzleToDelete by remember { mutableStateOf<CustomPuzzle?>(null) }

    puzzleToDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { puzzleToDelete = null },
            title = { Text("Törlés") },
            text  = { Text("\"${pending.name}\" véglegesen törlésre kerül.") },
            confirmButton = {
                Button(onClick = {
                    scope.launch { repo.delete(pending.id) }
                    puzzleToDelete = null
                }) { Text("Törlés") }
            },
            dismissButton = {
                TextButton(onClick = { puzzleToDelete = null }) { Text("Mégse") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Válassz rejtvényt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Vissza")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp)
        ) {
            item {
                SectionHeader("Előre összeállított rejtvények")
            }
            items(builtInPuzzles) { entity ->
                ListItem(
                    headlineContent = { Text(entity.name, fontSize = 17.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPuzzleSelected(PuzzleDefinition(entity.name, entity.boardString)) }
                        .padding(horizontal = 8.dp)
                )
                HorizontalDivider()
            }

            item {
                SectionHeader("Saját rejtvények")
            }
            if (customPuzzles.isEmpty()) {
                item {
                    Text(
                        text = "Még nincs saját rejtvény. Hozz létre egyet a szerkesztőben.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(customPuzzles, key = { it.id }) { puzzle ->
                    ListItem(
                        headlineContent = { Text(puzzle.name, fontSize = 17.sp) },
                        trailingContent = {
                            IconButton(onClick = { puzzleToDelete = puzzle }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Törlés",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPuzzleSelected(PuzzleDefinition(puzzle.name, puzzle.boardString))
                            }
                            .padding(horizontal = 8.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
