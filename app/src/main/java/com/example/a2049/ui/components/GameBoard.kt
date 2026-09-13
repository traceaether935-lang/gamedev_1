package com.example.a2049.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.a2049.ui.theme._2049Theme
import com.game.a2048.Direction

@Composable
fun GameBoard(
    grid: List<List<Int>>,
    onSwipe: (Direction) -> Unit,
    onTileClick: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    isSwipeEnabled: Boolean = true
) {
    val rows = grid.size
    val cols = if (rows > 0) grid[0].size else 0
    val safeCols = maxOf(1, cols)
    val safeRows = maxOf(1, rows)
    val maxDim = maxOf(safeRows, safeCols)

    val spacing = when {
        maxDim >= 10 -> 2.dp
        maxDim >= 8 -> 4.dp
        maxDim >= 6 -> 6.dp
        else -> 10.dp
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val cellWidth = maxWidth / safeCols
        val cellHeight = maxHeight / safeRows

        val cellSize = min(cellWidth, cellHeight)

        val boardWidth = cellSize * safeCols
        val boardHeight = cellSize * safeRows

        val boardCornerRadius = min(16.dp, cellSize * 0.3f)

        Box(
            modifier = Modifier
                .width(boardWidth)
                .height(boardHeight)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(boardCornerRadius))
                .clip(RoundedCornerShape(boardCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(spacing)
                .swipeDetector(enabled = isSwipeEnabled, onSwipe = onSwipe)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        for (col in 0 until cols) {
                            val tileValue = grid.getOrNull(row)?.getOrNull(col) ?: 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(
                                        end = if (col < cols - 1) spacing / 2 else 0.dp,
                                        start = if (col > 0) spacing / 2 else 0.dp,
                                        bottom = if (row < rows - 1) spacing / 2 else 0.dp,
                                        top = if (row > 0) spacing / 2 else 0.dp
                                    )
                                    .clickable { onTileClick(row, col) }
                            ) {
                                TileView(
                                    value = tileValue,
                                    boardSize = maxDim,
                                    cellSize = cellSize,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameBoardPreview() {
    _2049Theme {
        Box(modifier = Modifier.size(360.dp).padding(16.dp)) {
            GameBoard(
                grid = listOf(
                    listOf(2, 4, 8, 16),
                    listOf(32, 64, 128, 256),
                    listOf(512, 1024, 2048, 4096),
                    listOf(0, 0, 0, 0)
                ),
                onSwipe = {}
            )
        }
    }
}
