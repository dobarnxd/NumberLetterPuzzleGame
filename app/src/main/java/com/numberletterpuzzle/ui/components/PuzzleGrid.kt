package com.numberletterpuzzle.ui.components

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.numberletterpuzzle.data.Puzzle

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 4f

@Composable
fun PuzzleGrid(
    puzzle: Puzzle,
    userMapping: Map<Int, Char>,
    selectedNumber: Int?,
    onCellClick: (number: Int) -> Unit,
    modifier: Modifier = Modifier,
    preFilledNumbers: Set<Int> = emptySet(),
    wrongNumbers: Set<Int> = emptySet()
) {
    val numCols = if (puzzle.grid.isNotEmpty()) puzzle.grid[0].size else 1
    val numRows = if (puzzle.grid.isEmpty()) 1 else puzzle.grid.size
    val density = LocalDensity.current

    var containerW by remember { mutableFloatStateOf(0f) }
    var containerH by remember { mutableFloatStateOf(0f) }

    val containerWidthDp  = with(density) { containerW.toDp() }
    val containerHeightDp = with(density) { containerH.toDp() }

    val fittedCellSize = minOf(containerWidthDp / numCols, containerHeightDp / numRows)

    val boardWidthDp   = fittedCellSize * numCols
    val boardHeightDp  = fittedCellSize * numRows
    val boardWidthPx   = with(density) { boardWidthDp.toPx() }
    val boardHeightPx  = with(density) { boardHeightDp.toPx() }
    val centeredOffsetX = (containerW - boardWidthPx) / 2f
    val centeredOffsetY = (containerH - boardHeightPx) / 2f

    var scale   by remember(containerW, containerH, numCols, numRows) { mutableFloatStateOf(MIN_ZOOM) }
    var offsetX by remember(containerW, containerH, numCols, numRows) { mutableFloatStateOf(centeredOffsetX) }
    var offsetY by remember(containerW, containerH, numCols, numRows) { mutableFloatStateOf(centeredOffsetY) }

    // rememberUpdatedState azértt kell, hogy a tap handler mindig a friss értékeket olvassa
    val latestScale   by rememberUpdatedState(scale)
    val latestOffsetX by rememberUpdatedState(offsetX)
    val latestOffsetY by rememberUpdatedState(offsetY)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerW = size.width.toFloat()
                containerH = size.height.toFloat()
            }
            .clipToBounds()
            .pointerInput(puzzle, boardWidthPx, boardHeightPx, containerW, containerH) {
                detectTapGestures { tapOffset ->
                    val s  = latestScale
                    val ox = latestOffsetX
                    val oy = latestOffsetY

                    val boardX = (tapOffset.x - ox) / s
                    val boardY = (tapOffset.y - oy) / s

                    if (boardX < 0f || boardY < 0f ||
                        boardX > boardWidthPx || boardY > boardHeightPx
                    ) return@detectTapGestures

                    val cellWidthPx  = boardWidthPx  / numCols
                    val cellHeightPx = boardHeightPx / numRows
                    val col = (boardX / cellWidthPx).toInt().coerceIn(0, numCols - 1)
                    val row = (boardY / cellHeightPx).toInt().coerceIn(0, numRows - 1)

                    val cell = puzzle.grid[row][col]
                    if (!cell.isBlocked && cell.number != null) {
                        onCellClick(cell.number)
                    }
                }
            }
            .pointerInput(boardWidthPx, boardHeightPx, containerW, containerH) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val previousScale = scale
                    val newScale = (previousScale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    val actualZoom = newScale / previousScale

                    var newOffsetX = centroid.x * (1f - actualZoom) + offsetX * actualZoom + pan.x
                    var newOffsetY = centroid.y * (1f - actualZoom) + offsetY * actualZoom + pan.y

                    val scaledW = boardWidthPx * newScale
                    val scaledH = boardHeightPx * newScale

                    newOffsetX = clampOffset(newOffsetX, scaledW, containerW)
                    newOffsetY = clampOffset(newOffsetY, scaledH, containerH)

                    scale   = newScale
                    offsetX = newOffsetX
                    offsetY = newOffsetY
                }
            }
    ) {
        if (containerW > 0f && containerH > 0f) {
            Column(
                modifier = Modifier
                    .requiredSize(boardWidthDp, boardHeightDp)
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                for (row in puzzle.grid) {
                    Row {
                        for (cell in row) {
                            GridCell(
                                cell        = cell,
                                cellSize    = fittedCellSize,
                                userLetter  = if (!cell.isBlocked) userMapping[cell.number] else null,
                                isSelected  = !cell.isBlocked && cell.number == selectedNumber,
                                isPreFilled = !cell.isBlocked && cell.number != null && cell.number in preFilledNumbers,
                                isWrong     = !cell.isBlocked && cell.number != null && cell.number in wrongNumbers
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun clampOffset(offset: Float, scaledSize: Float, containerSize: Float): Float {
    return if (scaledSize <= containerSize) {
        (containerSize - scaledSize) / 2f
    } else {
        offset.coerceIn(containerSize - scaledSize, 0f)
    }
}
