package com.numberletterpuzzle.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numberletterpuzzle.data.PuzzleDatabase
import com.numberletterpuzzle.data.PuzzleStats
import com.numberletterpuzzle.data.StatisticsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember {
        StatisticsRepository(PuzzleDatabase.getDatabase(context).gameResultDao())
    }
    var stats by remember { mutableStateOf<PuzzleStats?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        stats = repository.getStats()
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Törlés megerősítése") },
            text = { Text("Biztosan törölni szeretnéd az összes statisztikát? Ez nem visszavonható.") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        stats = null
                        repository.clearStats()
                        stats = repository.getStats()
                    }
                    showClearConfirm = false
                }) { Text("Törlés") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Mégse") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statisztika") },
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
        if (stats == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatRow("Megoldott rejtvények",   stats!!.completedCount.toString())
                StatRow("Legjobb idő",            formatMillis(stats!!.bestTimeMillis))
                StatRow("Átlagos idő",            formatMillis(stats!!.averageTimeMillis))
                StatRow("Összes hiba",            stats!!.totalMistakes.toString())
                StatRow("Felhasznált tippek",     stats!!.totalHints.toString())
                StatRow("Megmutatott megoldások", stats!!.revealedCount.toString())
                StatRow("Utoljára játszott",      stats!!.lastPlayedPuzzle.ifEmpty { "—" })
                StatRow("Utolsó játék ideje",     formatMillis(stats!!.lastElapsedMillis))
                StatRow("Utolsó befejezés",       formatTimestamp(stats!!.lastCompletionTimestamp))

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Statisztika törlése")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider()
}

private fun formatMillis(millis: Long): String {
    if (millis <= 0L) return "—"
    val totalSeconds = millis / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

private fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "—"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
