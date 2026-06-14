package com.example.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    tooltipText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipFilledIconButton(
    tooltipText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        FilledIconButton(onClick = onClick, modifier = modifier, enabled = enabled, colors = colors, content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipFloatingActionButton(
    tooltipText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = FloatingActionButtonDefaults.containerColor,
    contentColor: androidx.compose.ui.graphics.Color = contentColorFor(containerColor),
    shape: androidx.compose.ui.graphics.Shape = FloatingActionButtonDefaults.shape,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        FloatingActionButton(onClick = onClick, modifier = modifier, containerColor = containerColor, contentColor = contentColor, shape = shape, content = content)
    }
}
