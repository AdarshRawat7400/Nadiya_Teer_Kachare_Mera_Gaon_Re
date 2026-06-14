package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedToggle(
    leftText: String,
    rightText: String,
    isRightSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val leftColor = if (!isRightSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        val leftTextColor = if (!isRightSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        val rightColor = if (isRightSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        val rightTextColor = if (isRightSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(leftColor)
                .clickable { onToggle(false) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(leftText, color = leftTextColor, style = MaterialTheme.typography.labelLarge)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(rightColor)
                .clickable { onToggle(true) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(rightText, color = rightTextColor, style = MaterialTheme.typography.labelLarge)
        }
    }
}
