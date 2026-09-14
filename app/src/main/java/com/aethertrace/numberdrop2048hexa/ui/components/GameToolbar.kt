package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme

@Composable
fun GameToolbar(
    undoUses: Int,
    hammerUses: Int,
    switchUses: Int,
    activeTool: ActiveTool,
    onUndoClick: () -> Unit,
    onHammerClick: () -> Unit,
    onSwitchClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                icon = Icons.AutoMirrored.Rounded.Undo,
                label = "Undo",
                uses = undoUses,
                isActive = false,
                onClick = onUndoClick,
                isEnabled = isEnabled
            )

            ToolButton(
                icon = Icons.Rounded.Build,
                label = "Hammer",
                uses = hammerUses,
                isActive = activeTool == ActiveTool.HAMMER,
                onClick = onHammerClick,
                isEnabled = isEnabled
            )

            ToolButton(
                icon = Icons.Rounded.SwapHoriz,
                label = "Switch",
                uses = switchUses,
                isActive = activeTool == ActiveTool.SWITCH,
                onClick = onSwitchClick,
                isEnabled = isEnabled
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    uses: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val border = if (isActive) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    BadgedBox(
        badge = {
            if (uses > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "$uses",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            } else {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Watch Ad (+2)",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            enabled = isEnabled,
            shape = RoundedCornerShape(12.dp),
            color = containerColor,
            contentColor = contentColor,
            border = border,
            modifier = Modifier.size(width = 88.dp, height = 56.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun GameToolbarPreview() {
    _2049Theme {
        GameToolbar(
            undoUses = 2,
            hammerUses = 0,
            switchUses = 1,
            activeTool = ActiveTool.HAMMER,
            onUndoClick = {},
            onHammerClick = {},
            onSwitchClick = {}
        )
    }
}
