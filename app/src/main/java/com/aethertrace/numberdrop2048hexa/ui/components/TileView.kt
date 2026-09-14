package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.aethertrace.numberdrop2048hexa.ui.theme.LocalAppThemeMode
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme
import com.aethertrace.numberdrop2048hexa.ui.theme.getTileColors

fun getTileFontSize(value: Int, cellSize: Dp): TextUnit {
    val sizePx = cellSize.value
    val digits = value.toString().length
    val factor = when {
        digits <= 2 -> 0.42f
        digits == 3 -> 0.35f
        digits == 4 -> 0.28f
        digits == 5 -> 0.22f
        else -> 0.18f
    }
    return maxOf(sizePx * factor, 6f).sp
}

fun getTileFontSize(value: Int, boardSize: Int): TextUnit {
    val estimatedCellSize = (320f / maxOf(1, boardSize)).dp
    return getTileFontSize(value, estimatedCellSize)
}

@Composable
fun TileView(
    value: Int,
    boardSize: Int = 4,
    modifier: Modifier = Modifier,
    cellSize: Dp = Dp.Unspecified
) {
    if (cellSize.isSpecified && cellSize > 0.dp) {
        TileContent(
            value = value,
            cellSize = cellSize,
            modifier = modifier
        )
    } else {
        BoxWithConstraints(modifier = modifier) {
            TileContent(
                value = value,
                cellSize = maxWidth,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TileContent(
    value: Int,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = min(12.dp, cellSize * 0.18f)
    val shape = RoundedCornerShape(cornerRadius)

    if (value == 0) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )
    } else {
        val scale = remember(value) { Animatable(0.4f) }

        LaunchedEffect(value) {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = 600f
                )
            )
        }

        val themeMode = LocalAppThemeMode.current
        val colors = getTileColors(value, themeMode)
        val fontSize = getTileFontSize(value, cellSize)
        val shadowElevation = min(4.dp, cellSize * 0.08f)

        Box(
            modifier = modifier
                .scale(scale.value)
                .shadow(elevation = shadowElevation, shape = shape)
                .clip(shape)
                .background(colors.background)
                .semantics {
                    contentDescription = "Tile with value $value"
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                color = colors.text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TileViewPreview() {
    _2049Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            TileView(value = 2048, boardSize = 4)
        }
    }
}
