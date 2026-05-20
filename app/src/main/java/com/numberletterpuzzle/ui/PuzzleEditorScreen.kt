package com.numberletterpuzzle.ui

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numberletterpuzzle.data.CustomPuzzle
import com.numberletterpuzzle.data.CustomPuzzleRepository
import com.numberletterpuzzle.data.PuzzleDatabase
import com.numberletterpuzzle.ui.components.HungarianKeyboard
import com.numberletterpuzzle.ui.components.HUNGARIAN_KEYBOARD_ROWS
import com.numberletterpuzzle.ui.theme.LocalPuzzleColors
import kotlinx.coroutines.launch
import java.util.UUID

private data class EditorCell(
    val isBlocked: Boolean = false,
    val letter: Char? = null
)

private sealed class ImportResult {
    data class Failure(val message: String) : ImportResult()
    data class Success(
        val cells: List<List<EditorCell>>,
        val rows: Int,
        val cols: Int,
        val warning: String? = null
    ) : ImportResult()
}

private val VALID_BOARD_CHARS: Set<Char> = buildValidChars()

private fun buildValidChars(): Set<Char> {
    val result = mutableSetOf<Char>()
    for (row in HUNGARIAN_KEYBOARD_ROWS) {
        for (ch in row) result.add(ch)
    }
    result.add('#')
    return result
}

private fun emptyGrid(rows: Int, cols: Int): List<List<EditorCell>> {
    val grid = mutableListOf<List<EditorCell>>()
    for (r in 0 until rows) {
        val row = mutableListOf<EditorCell>()
        for (c in 0 until cols) row.add(EditorCell())
        grid.add(row)
    }
    return grid
}

private fun resizeGrid(
    cells: List<List<EditorCell>>,
    newRows: Int,
    newCols: Int
): List<List<EditorCell>> {
    val result = mutableListOf<List<EditorCell>>()
    for (r in 0 until newRows) {
        val row = mutableListOf<EditorCell>()
        for (c in 0 until newCols) {
            val cell = if (r < cells.size && c < cells[r].size) cells[r][c] else EditorCell()
            row.add(cell)
        }
        result.add(row)
    }
    return result
}

private fun buildBoardString(cells: List<List<EditorCell>>): String {
    val sb = StringBuilder()
    for (i in cells.indices) {
        for (cell in cells[i]) {
            sb.append(if (cell.isBlocked) '#' else cell.letter ?: '#')
        }
        if (i < cells.size - 1) sb.append('\n')
    }
    return sb.toString()
}

private fun validateGrid(cells: List<List<EditorCell>>): String? {
    var activeCount = 0
    var emptyCount = 0
    for (row in cells) {
        for (cell in row) {
            if (!cell.isBlocked) {
                activeCount++
                if (cell.letter == null) emptyCount++
            }
        }
    }
    if (activeCount == 0) return "A táblának legalább egy aktív cellát kell tartalmaznia."
    if (emptyCount > 0) return "$emptyCount cellán hiányzik a betű. Töltsd ki vagy blokkold mentés előtt."
    return null
}

private fun parseImportText(text: String): ImportResult {
    val lines = text.trim().lines().map { it.trimEnd() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return ImportResult.Failure("A tábla szövege üres.")

    val invalid = lines.flatMap { it.asIterable() }.filter { it !in VALID_BOARD_CHARS }.toSet()
    if (invalid.isNotEmpty()) {
        val display = invalid.sorted().joinToString(", ") { "'$it'" }
        return ImportResult.Failure("Érvénytelen karakterek: $display. Csak magyar betűk és # engedélyezett.")
    }

    val maxLen = lines.maxOf { it.length }
    val lengthSet = lines.map { it.length }.toSet()
    val warning = if (lengthSet.size > 1) {
        "A sorok különböző hosszúak (${lengthSet.sorted().joinToString(", ")}). A rövidebb sorok # jellel lesznek kiegészítve."
    } else null

    val cells = lines.map { line ->
        line.padEnd(maxLen, '#').map { ch ->
            if (ch == '#') EditorCell(isBlocked = true) else EditorCell(letter = ch)
        }
    }

    val activeCount = cells.sumOf { row -> row.count { !it.isBlocked } }
    if (activeCount == 0) return ImportResult.Failure("A táblának legalább egy betűs cellát kell tartalmaznia.")

    return ImportResult.Success(cells, lines.size, maxLen, warning)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { CustomPuzzleRepository(PuzzleDatabase.getDatabase(context).puzzleDao()) }

    var rows by remember { mutableIntStateOf(7) }
    var cols by remember { mutableIntStateOf(7) }
    var cells by remember { mutableStateOf(emptyGrid(7, 7)) }

    var activeTab by remember { mutableIntStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var savedBanner by remember { mutableStateOf(false) }

    if (showSaveDialog) {
        SavePuzzleDialog(
            onSave = { name ->
                scope.launch {
                    repo.save(
                        CustomPuzzle(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            boardString = buildBoardString(cells),
                            createdTimestamp = System.currentTimeMillis()
                        )
                    )
                }
                showSaveDialog = false
                savedBanner = true
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rejtvényszerkesztő") },
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (savedBanner) SavedBanner(onDismiss = { savedBanner = false })

            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("Rácsszerkesztő", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Szöveges bevitel", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            when (activeTab) {
                0 -> GridEditorTab(
                    rows = rows,
                    cols = cols,
                    cells = cells,
                    onRowsChange = { rows = it },
                    onColsChange = { cols = it },
                    onCellsChange = { cells = it },
                    onSaveRequest = { showSaveDialog = true }
                )
                1 -> TextImportTab { newCells, newRows, newCols ->
                    cells = newCells
                    rows = newRows
                    cols = newCols
                    activeTab = 0
                }
            }
        }
    }
}

@Composable
private fun SavePuzzleDialog(onSave: (name: String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mentés") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Rejtvény neve") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                nameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmed = name.trim()
                if (trimmed.isBlank()) { nameError = "A név nem lehet üres."; return@Button }
                onSave(trimmed)
            }) { Text("Mentés") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégse") } }
    )
}

@Composable
private fun SavedBanner(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF388E3C))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Mentve! Megjelenik a rejtvénylistában.", color = Color.White, fontSize = 14.sp)
        TextButton(onClick = onDismiss) {
            Text("×", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
private fun GridEditorTab(
    rows: Int,
    cols: Int,
    cells: List<List<EditorCell>>,
    onRowsChange: (Int) -> Unit,
    onColsChange: (Int) -> Unit,
    onCellsChange: (List<List<EditorCell>>) -> Unit,
    onSaveRequest: () -> Unit
) {
    var selRow by remember { mutableStateOf<Int?>(null) }
    var selCol by remember { mutableStateOf<Int?>(null) }
    var gridError by remember { mutableStateOf<String?>(null) }

    fun updateCell(r: Int, c: Int, transform: (EditorCell) -> EditorCell) {
        onCellsChange(cells.mapIndexed { ri, row ->
            if (ri != r) row else row.mapIndexed { ci, cell -> if (ci == c) transform(cell) else cell }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("Sorok:", fontSize = 13.sp)
            StepButton("−") {
                if (rows > 3) {
                    val newRows = rows - 1
                    onRowsChange(newRows)
                    onCellsChange(resizeGrid(cells, newRows, cols))
                    if ((selRow ?: 0) >= newRows) selRow = null
                }
            }
            Text("$rows", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            StepButton("+") {
                if (rows < 25) { val newRows = rows + 1; onRowsChange(newRows); onCellsChange(resizeGrid(cells, newRows, cols)) }
            }
            Spacer(Modifier.width(12.dp))
            Text("Oszlopok:", fontSize = 13.sp)
            StepButton("−") {
                if (cols > 3) {
                    val newCols = cols - 1
                    onColsChange(newCols)
                    onCellsChange(resizeGrid(cells, rows, newCols))
                    if ((selCol ?: 0) >= newCols) selCol = null
                }
            }
            Text("$cols", modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            StepButton("+") {
                if (cols < 25) { val newCols = cols + 1; onColsChange(newCols); onCellsChange(resizeGrid(cells, rows, newCols)) }
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = { onCellsChange(emptyGrid(rows, cols)); selRow = null; selCol = null; gridError = null },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Mindent töröl", fontSize = 11.sp)
            }
        }

        HorizontalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(LocalPuzzleColors.current.gridBackground)
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                for (r in 0 until rows) {
                    Row {
                        for (c in 0 until cols) {
                            EditorCellView(
                                cell = cells[r][c],
                                isSelected = r == selRow && c == selCol,
                                onClick = { selRow = r; selCol = c; gridError = null }
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        val sr = selRow
        val sc = selCol
        if (sr != null && sc != null) {
            val selCell = cells[sr][sc]
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        updateCell(sr, sc) { cell ->
                            if (cell.isBlocked) cell.copy(isBlocked = false)
                            else cell.copy(isBlocked = true, letter = null)
                        }
                        gridError = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (selCell.isBlocked) "Felold" else "Blokkol", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { updateCell(sr, sc) { it.copy(letter = null) }; gridError = null },
                    modifier = Modifier.weight(1f),
                    enabled = !selCell.isBlocked && selCell.letter != null
                ) {
                    Text("Betű törlése", fontSize = 13.sp)
                }
            }
            if (!selCell.isBlocked) {
                val usedInGrid = cells.flatMap { row -> row.mapNotNull { if (!it.isBlocked) it.letter else null } }.toSet()
                HungarianKeyboard(
                    usedLetters = usedInGrid,
                    enabled = true,
                    onLetterClick = { letter ->
                        updateCell(sr, sc) { it.copy(isBlocked = false, letter = letter) }
                        val nextC = sc + 1
                        if (nextC < cols) selCol = nextC
                        else if (sr + 1 < rows) { selRow = sr + 1; selCol = 0 }
                        gridError = null
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Kattints egy cellára, majd válassz betűt vagy blokkold",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        HorizontalDivider()

        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            gridError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            Button(
                onClick = {
                    val err = validateGrid(cells)
                    if (err != null) { gridError = err; return@Button }
                    gridError = null
                    onSaveRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mentés")
            }
        }
    }
}

@Composable
private fun TextImportTab(onImportSuccess: (cells: List<List<EditorCell>>, rows: Int, cols: Int) -> Unit) {
    var importText by remember { mutableStateOf("") }
    var importMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Illeszd be a táblát. Minden sor egy sort jelent.\nHasználj # jelet a blokkolt cellákhoz, betűket az aktív cellákhoz.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
        OutlinedTextField(
            value = importText,
            onValueChange = { importText = it; importMsg = null },
            label = { Text("Tábla szövege") },
            modifier = Modifier.fillMaxWidth().height(180.dp),
            maxLines = 30,
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        )
        importMsg?.let { msg ->
            Text(
                text = msg,
                fontSize = 12.sp,
                color = if (msg.startsWith("⚠")) MaterialTheme.colorScheme.error else Color(0xFF388E3C),
                lineHeight = 16.sp
            )
        }
        Button(
            onClick = {
                when (val result = parseImportText(importText)) {
                    is ImportResult.Failure -> importMsg = "⚠ ${result.message}"
                    is ImportResult.Success -> {
                        importMsg = result.warning?.let { "⚠ $it" }
                        onImportSuccess(result.cells, result.rows, result.cols)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = importText.isNotBlank()
        ) {
            Text("Importálás")
        }
        HorizontalDivider()
        Text("Érvényes karakterek", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Betűk: a á b c d e é f g h i í j k l m n o ó ö ő p q r s t u ú ü ű v w x y z\nBlokkolt: #",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp,
            fontFamily = FontFamily.Monospace
        )
        Text("Példa", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "alma#körte\n#barack##\neper#szilva",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EditorCellView(cell: EditorCell, isSelected: Boolean, onClick: () -> Unit) {
    val puzzleColors = LocalPuzzleColors.current
    val bgColor = when {
        isSelected && cell.isBlocked -> Color(0xFF37474F)
        isSelected -> puzzleColors.selectedCell
        cell.isBlocked -> puzzleColors.blockedCell
        else -> puzzleColors.cellBackground
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) puzzleColors.selectedBorder else puzzleColors.cellBorder)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isBlocked -> Text("×", color = if (isSelected) Color(0xFFB0BEC5) else Color(0xFF777777), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            cell.letter != null -> Text(cell.letter.uppercase(), color = if (isSelected) puzzleColors.selectedBorder else puzzleColors.letterColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            else -> Text("·", color = puzzleColors.cellBorder, fontSize = 22.sp)
        }
    }
}
