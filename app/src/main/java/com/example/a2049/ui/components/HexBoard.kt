package com.example.a2049.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2049.ui.theme.AppThemeMode
import com.example.a2049.ui.theme.getTileColors
import com.game.a2048.hex.HexCell
import com.game.a2048.hex.HexGrid
import com.game.a2048.hex.PowerUpType
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

fun findPlacementCellsForBoard(
    activePieceSize: Int,
    targetCell: HexCell,
    grid: Map<HexCell, Int>,
    radius: Int
): List<HexCell>? {
    if (!HexGrid.contains(targetCell, radius)) return null
    if ((grid[targetCell] ?: 0) != 0) return null
    if (activePieceSize <= 1) return listOf(targetCell)

    val cell2 = targetCell.neighbors().firstOrNull {
        HexGrid.contains(it, radius) && (grid[it] ?: 0) == 0
    } ?: return null

    if (activePieceSize == 2) return listOf(targetCell, cell2)

    val cell3 = targetCell.neighbors().firstOrNull { n ->
        HexGrid.contains(n, radius) && (grid[n] ?: 0) == 0 && n != cell2 && cell2.neighbors().contains(n)
    } ?: targetCell.neighbors().firstOrNull { n ->
        HexGrid.contains(n, radius) && (grid[n] ?: 0) == 0 && n != cell2
    } ?: cell2.neighbors().firstOrNull { n ->
        HexGrid.contains(n, radius) && (grid[n] ?: 0) == 0 && n != targetCell
    } ?: return null

    return listOf(targetCell, cell2, cell3)
}

fun findCellAtOffset(offset: Offset, width: Float, height: Float, radius: Int = HexGrid.DEFAULT_RADIUS): HexCell? {
    val maxRowSpan = 2 * radius + 1
    val maxRowWidthFactor = maxRowSpan * sqrt(3.0f)
    val maxRowHeightFactor = 3 * radius + 2.0f
    val verticalPaddingPx = min(width, height) * 0.06f

    val availableWidth = (width - verticalPaddingPx * 2).coerceAtLeast(1f)
    val availableHeight = (height - verticalPaddingPx * 2).coerceAtLeast(1f)

    val hexRadiusPx = min(
        availableWidth / maxRowWidthFactor,
        availableHeight / maxRowHeightFactor
    )
    val centerX = width / 2f
    val centerY = height / 2f

    var closestCell: HexCell? = null
    var minDistance = Float.MAX_VALUE

    for (cell in HexGrid.getCells(radius)) {
        val cx = centerX + hexRadiusPx * sqrt(3.0f) * (cell.q + cell.r / 2.0f)
        val cy = centerY + hexRadiusPx * 1.5f * cell.r
        val dist = sqrt((offset.x - cx) * (offset.x - cx) + (offset.y - cy) * (offset.y - cy))
        if (dist <= hexRadiusPx * 1.05f && dist < minDistance) {
            minDistance = dist
            closestCell = cell
        }
    }
    return closestCell
}

@Composable
fun HexBoard(
    grid: Map<HexCell, Int>,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier,
    highlightedCell: HexCell? = null,
    activePieceSize: Int = 1,
    radius: Int = HexGrid.DEFAULT_RADIUS,
    selectedSwapCell: HexCell? = null,
    activePowerUp: PowerUpType? = null,
    onCellClick: ((HexCell) -> Unit)? = null,
    onBoardPositioned: ((positionInRoot: Offset, width: Float, height: Float) -> Unit)? = null,
    themeMode: AppThemeMode = AppThemeMode.DEFAULT
) {
    val textMeasurer = rememberTextMeasurer()
    val boardBgColor = MaterialTheme.colorScheme.surfaceVariant
    val slotBgColor = Color(0x33888888)
    val primaryHighlight = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(boardBgColor)
            .padding(12.dp)
            .onGloballyPositioned { coordinates ->
                onBoardPositioned?.invoke(
                    coordinates.positionInRoot(),
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isInteractive, onCellClick, radius) {
                    if (!isInteractive) return@pointerInput

                    detectTapGestures(
                        onTap = { tapOffset ->
                            val cell = findCellAtOffset(tapOffset, size.width.toFloat(), size.height.toFloat(), radius)
                            if (cell != null) {
                                onCellClick?.invoke(cell)
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val maxRowSpan = 2 * radius + 1
            val maxRowWidthFactor = maxRowSpan * sqrt(3.0f)
            val maxRowHeightFactor = 3 * radius + 2.0f
            val verticalPaddingPx = min(width, height) * 0.06f

            val availableWidth = (width - verticalPaddingPx * 2).coerceAtLeast(1f)
            val availableHeight = (height - verticalPaddingPx * 2).coerceAtLeast(1f)

            val hexRadiusPx = min(
                availableWidth / maxRowWidthFactor,
                availableHeight / maxRowHeightFactor
            )
            val tileR = hexRadiusPx * 0.90f

            val centerX = width / 2f
            val centerY = height / 2f

            val targetCells = if (activePowerUp == null && highlightedCell != null && (grid[highlightedCell] ?: 0) == 0) {
                findPlacementCellsForBoard(activePieceSize, highlightedCell, grid, radius) ?: emptyList()
            } else emptyList()

            for (cell in HexGrid.getCells(radius)) {
                val cx = centerX + hexRadiusPx * sqrt(3.0f) * (cell.q + cell.r / 2.0f)
                val cy = centerY + hexRadiusPx * 1.5f * cell.r

                val hexPath = Path()
                for (k in 0..5) {
                    val angleRad = Math.toRadians(60.0 * k - 30.0)
                    val vx = (cx + tileR * cos(angleRad)).toFloat()
                    val vy = (cy + tileR * sin(angleRad)).toFloat()
                    if (k == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                }
                hexPath.close()

                val tileVal = grid[cell] ?: 0
                val tileColors = getTileColors(tileVal, themeMode)
                val isTarget = (activePowerUp == null) && cell in targetCells && tileVal == 0
                val isSwapSelected = (activePowerUp == PowerUpType.SWAP && cell == selectedSwapCell)
                val isHammerTarget = (activePowerUp == PowerUpType.HAMMER && cell == highlightedCell && tileVal != 0)

                if (tileVal == 0) {
                    if (isTarget) {
                        drawPath(
                            path = hexPath,
                            color = primaryHighlight.copy(alpha = 0.35f)
                        )
                        drawPath(
                            path = hexPath,
                            color = primaryHighlight,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    } else {
                        drawPath(
                            path = hexPath,
                            color = slotBgColor
                        )
                    }
                } else {
                    drawPath(
                        path = hexPath,
                        color = tileColors.background
                    )

                    val textStr = tileVal.toString()
                    val fontSizeSp = when {
                        textStr.length <= 2 -> (tileR * 0.52f).sp
                        textStr.length == 3 -> (tileR * 0.42f).sp
                        else -> (tileR * 0.34f).sp
                    }

                    val textLayoutResult = textMeasurer.measure(
                        text = textStr,
                        style = TextStyle(
                            color = tileColors.text,
                            fontSize = fontSizeSp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            cx - textLayoutResult.size.width / 2f,
                            cy - textLayoutResult.size.height / 2f
                        )
                    )

                    if (isSwapSelected) {
                        drawPath(
                            path = hexPath,
                            color = primaryHighlight,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    } else if (isHammerTarget) {
                        drawPath(
                            path = hexPath,
                            color = Color(0x66FF3333)
                        )
                        drawPath(
                            path = hexPath,
                            color = Color.Red,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
