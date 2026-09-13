package com.example.a2049.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.game.a2048.hex.HexPiece
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun UpcomingPiecesTray(
    upcomingPieces: List<HexPiece>,
    selectedPieceIndex: Int?,
    onPieceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onPieceDragStart: (Int, Offset) -> Unit = { _, _ -> },
    onPieceDrag: (Int, Offset) -> Unit = { _, _ -> },
    onPieceDragEnd: (Int) -> Unit = {},
    themeMode: AppThemeMode = AppThemeMode.DEFAULT
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "UPCOMING PIECES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                upcomingPieces.forEachIndexed { index, piece ->
                    val isSelected = selectedPieceIndex == index
                    PieceCard(
                        piece = piece,
                        isSelected = isSelected,
                        onClick = { onPieceSelected(index) },
                        onDragStart = { offset -> onPieceDragStart(index, offset) },
                        onDrag = { offset -> onPieceDrag(index, offset) },
                        onDragEnd = { onPieceDragEnd(index) },
                        themeMode = themeMode
                    )
                }
            }
        }
    }
}

@Composable
private fun PieceCard(
    piece: HexPiece,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    themeMode: AppThemeMode
) {
    val textMeasurer = rememberTextMeasurer()
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    var rootPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else Color(0x22888888)
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .onGloballyPositioned { coordinates ->
                rootPosition = coordinates.positionInRoot()
            }
            .clickable { onClick() }
            .pointerInput(piece.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        val globalOffset = rootPosition + localOffset
                        onDragStart(globalOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val globalOffset = rootPosition + change.position
                        onDrag(globalOffset)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(68.dp)) {
            val width = size.width
            val height = size.height
            val values = piece.values

            if (values.size == 1) {
                // Single Hex Tile
                val tileVal = values[0]
                val tileColors = getTileColors(tileVal, themeMode)
                val cx = width / 2f
                val cy = height / 2f
                val tileR = height * 0.42f

                val hexPath = Path()
                for (k in 0..5) {
                    val angleRad = Math.toRadians(60.0 * k - 30.0)
                    val vx = (cx + tileR * cos(angleRad)).toFloat()
                    val vy = (cy + tileR * sin(angleRad)).toFloat()
                    if (k == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                }
                hexPath.close()

                drawPath(path = hexPath, color = tileColors.background)

                val textStr = tileVal.toString()
                val fontSizeSp = (tileR * 0.55f).sp
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
            } else if (values.size == 2) {
                // Pair Hex Tiles (2 connected adjacent hexagons sharing a vertical edge side-by-side)
                val tileR = min(width, height) * 0.25f
                val dx = sqrt(3.0f) * tileR
                val cy = height / 2f
                val centers = listOf(
                    Offset(width / 2f - dx / 2f, cy),
                    Offset(width / 2f + dx / 2f, cy)
                )

                values.take(2).forEachIndexed { idx, tileVal ->
                    val tileColors = getTileColors(tileVal, themeMode)
                    val cx = centers[idx].x

                    val hexPath = Path()
                    for (k in 0..5) {
                        val angleRad = Math.toRadians(60.0 * k - 30.0)
                        val vx = (cx + tileR * cos(angleRad)).toFloat()
                        val vy = (cy + tileR * sin(angleRad)).toFloat()
                        if (k == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                    }
                    hexPath.close()

                    drawPath(path = hexPath, color = tileColors.background)

                    val textStr = tileVal.toString()
                    val fontSizeSp = (tileR * 0.55f).sp
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
                }
            } else {
                // Triple Hex Tiles (3 connected adjacent hexagons in a joined cluster layout)
                val tileR = min(width, height) * 0.20f
                val dx = sqrt(3.0f) * tileR
                val dy = 1.5f * tileR
                val centers = listOf(
                    Offset(width / 2f - dx / 2f, height / 2f + dy / 3f),
                    Offset(width / 2f + dx / 2f, height / 2f + dy / 3f),
                    Offset(width / 2f, height / 2f - 2f * dy / 3f)
                )

                values.take(3).forEachIndexed { idx, tileVal ->
                    val tileColors = getTileColors(tileVal, themeMode)
                    val cx = centers[idx].x
                    val cy = centers[idx].y

                    val hexPath = Path()
                    for (k in 0..5) {
                        val angleRad = Math.toRadians(60.0 * k - 30.0)
                        val vx = (cx + tileR * cos(angleRad)).toFloat()
                        val vy = (cy + tileR * sin(angleRad)).toFloat()
                        if (k == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                    }
                    hexPath.close()

                    drawPath(path = hexPath, color = tileColors.background)

                    val textStr = tileVal.toString()
                    val fontSizeSp = (tileR * 0.55f).sp
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
                }
            }
        }
    }
}
