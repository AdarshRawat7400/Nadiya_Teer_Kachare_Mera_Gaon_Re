package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AudiobookPlayerViewModel

@Composable
fun NowPlayingMiniPlayer(
    viewModel: AudiobookPlayerViewModel,
    onNavigateToPlayer: () -> Unit,
    onClose: () -> Unit
) {
    val currentChapter by viewModel.currentChapter.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val duration by viewModel.duration.collectAsState()

    if (currentChapter == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onNavigateToPlayer() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    val cleanTitle = currentChapter?.title?.replace(Regex("^\\d+[_\\s-]*"), "") ?: ""
                    Text(
                        text = cleanTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                }

                IconButton(onClick = { viewModel.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous")
                }
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Play/Pause"
                    )
                }
                IconButton(onClick = { viewModel.playNext() }) {
                    Icon(Icons.Default.SkipNext, "Next")
                }
                IconButton(onClick = { 
                    viewModel.togglePlayPause() // optional stop logic
                    onClose()
                }) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            val progressPercent = if (duration > 0) (progress.toFloat() / duration.toFloat()) else 0f
            LinearProgressIndicator(
                progress = { progressPercent.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
