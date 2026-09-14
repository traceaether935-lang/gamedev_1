package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethertrace.numberdrop2048hexa.ui.model.FeedbackEvent
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun FloatingFeedbackText(
    feedbackEvent: FeedbackEvent?,
    modifier: Modifier = Modifier
) {
    var currentEvent by remember { mutableStateOf<FeedbackEvent?>(null) }
    val scale = remember { Animatable(0.4f) }
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(feedbackEvent?.id) {
        if (feedbackEvent != null) {
            currentEvent = feedbackEvent
            scale.snapTo(0.4f)
            offsetY.snapTo(0f)
            alpha.snapTo(1f)

            coroutineScope {
                // Scale animation: pop up quickly to 1.2 then settle to 1.0
                launch {
                    scale.animateTo(
                        targetValue = 1.25f,
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    )
                    scale.animateTo(
                        targetValue = 1.0f,
                        animationSpec = tween(durationMillis = 200)
                    )
                }

                // Float upward animation (-120px over 1500ms)
                launch {
                    offsetY.animateTo(
                        targetValue = -120f,
                        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
                    )
                }

                // Fade out animation (last 500ms of the 1500ms duration)
                launch {
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 600, delayMillis = 900)
                    )
                }
            }
            currentEvent = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        currentEvent?.let { event ->
            val textStyle = when (event.tier) {
                1 -> TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
                2 -> TextStyle(
                    color = Color(0xFFFFB300), // Gold / Amber
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(3f, 3f),
                        blurRadius = 6f
                    )
                )
                3 -> TextStyle(
                    color = Color(0xFFE040FB), // Epic Magenta / Purple
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(
                        color = Color(0xFF7B1FA2).copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = 12f
                    )
                )
                4 -> TextStyle(
                    color = Color(0xFFFF3D00), // Fiery Orange / Red Combo
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(
                        color = Color(0xFFFF9100).copy(alpha = 0.95f),
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                )
                else -> TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = event.text,
                style = textStyle,
                modifier = Modifier
                    .padding(8.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationY = offsetY.value
                        this.alpha = alpha.value
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloatingFeedbackTextPreview() {
    _2049Theme {
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingFeedbackText(
                feedbackEvent = FeedbackEvent(1L, "UNSTOPPABLE!", 4)
            )
        }
    }
}
