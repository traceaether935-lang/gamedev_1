package com.example.a2049.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val TARGET_GOAL_OPTIONS = listOf(32, 64, 128, 256, 512, 1024, 2048, 4096, 8192)

@Composable
fun GridSizeSelector(
    rows: Int,
    cols: Int,
    onRowsChange: (Int) -> Unit,
    onColsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    targetGoal: Int = 2048,
    onTargetGoalChange: ((Int) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Rows: $rows", fontWeight = FontWeight.Bold)
            Text(text = "Cols: $cols", fontWeight = FontWeight.Bold)
        }

        Slider(
            value = rows.toFloat(),
            onValueChange = { onRowsChange(it.toInt()) },
            valueRange = 2f..12f,
            steps = 9
        )
        Slider(
            value = cols.toFloat(),
            onValueChange = { onColsChange(it.toInt()) },
            valueRange = 2f..12f,
            steps = 9
        )

        Text(
            text = "Board Size: $rows x $cols",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (onTargetGoalChange != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Goal:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$targetGoal",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TARGET_GOAL_OPTIONS) { goal ->
                        FilterChip(
                            selected = (goal == targetGoal),
                            onClick = { onTargetGoalChange(goal) },
                            label = {
                                Text(
                                    text = "$goal",
                                    fontWeight = if (goal == targetGoal) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
