package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.game.a2048.Direction
import kotlin.math.abs

fun Modifier.swipeDetector(
    enabled: Boolean = true,
    minSwipeDistanceDp: Dp = 24.dp,
    onSwipe: (Direction) -> Unit
): Modifier = composed {
    if (!enabled) return@composed this

    val density = LocalDensity.current
    val minSwipeDistancePx = with(density) { minSwipeDistanceDp.toPx() }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var hasSwipedInCurrentGesture by remember { mutableStateOf(false) }

    pointerInput(enabled) {
        detectDragGestures(
            onDragStart = {
                totalDragX = 0f
                totalDragY = 0f
                hasSwipedInCurrentGesture = false
            },
            onDragEnd = {
                totalDragX = 0f
                totalDragY = 0f
                hasSwipedInCurrentGesture = false
            },
            onDragCancel = {
                totalDragX = 0f
                totalDragY = 0f
                hasSwipedInCurrentGesture = false
            },
            onDrag = { change, dragAmount ->
                change.consume()
                if (hasSwipedInCurrentGesture) return@detectDragGestures

                totalDragX += dragAmount.x
                totalDragY += dragAmount.y

                val absX = abs(totalDragX)
                val absY = abs(totalDragY)

                if (absX >= minSwipeDistancePx || absY >= minSwipeDistancePx) {
                    hasSwipedInCurrentGesture = true
                    val direction = if (absX > absY) {
                        if (totalDragX > 0) Direction.RIGHT else Direction.LEFT
                    } else {
                        if (totalDragY > 0) Direction.DOWN else Direction.UP
                    }
                    onSwipe(direction)
                }
            }
        )
    }
}
