package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ChapterAudio
import com.example.ui.components.SegmentedToggle

import com.example.ui.screens.BookViewModel

import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookPlayerScreen(
    viewModel: AudiobookPlayerViewModel,
    bookViewModel: BookViewModel,
    onBack: () -> Unit
) {
    val currentChapter by viewModel.currentChapter.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isFemaleVoice by viewModel.isFemaleVoice.collectAsState()
    val isExplainerActive by viewModel.isExplainerActive.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()

    val themeMode by bookViewModel.themeMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audiobook") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close Player")
                    }
                },
                actions = {
                    com.example.ui.components.TooltipIconButton(
                        tooltipText = "थीम बदलें",
                        onClick = { 
                        val next = when (themeMode) {
                            com.example.ui.theme.AppThemeMode.SYSTEM -> com.example.ui.theme.AppThemeMode.LIGHT
                            com.example.ui.theme.AppThemeMode.LIGHT -> com.example.ui.theme.AppThemeMode.DARK
                            com.example.ui.theme.AppThemeMode.DARK -> com.example.ui.theme.AppThemeMode.SEPIA
                            com.example.ui.theme.AppThemeMode.SEPIA -> com.example.ui.theme.AppThemeMode.SYSTEM
                        }
                        bookViewModel.setThemeMode(next) 
                    }) {
                        val icon = when (themeMode) {
                            com.example.ui.theme.AppThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness
                            com.example.ui.theme.AppThemeMode.LIGHT -> Icons.Filled.LightMode
                            com.example.ui.theme.AppThemeMode.DARK -> Icons.Filled.DarkMode
                            com.example.ui.theme.AppThemeMode.SEPIA -> Icons.Filled.Palette
                        }
                        Icon(icon, contentDescription = "Toggle Theme")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 1:1 Cover Art
            AsyncImage(
                model = currentChapter?.illustration_url,
                contentDescription = "Cover Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(32.dp))

            val cleanTitle = currentChapter?.title?.replace(Regex("^\\d+[_\\s-]*"), "") ?: "No Title"

            // Title
            Text(
                text = cleanTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggles
            if (currentChapter?.type == "poem") {
                SegmentedToggle(
                    leftText = "Original",
                    rightText = "Explainer",
                    isRightSelected = isExplainerActive,
                    onToggle = { viewModel.setExplainer(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            SegmentedToggle(
                leftText = "Male Voice",
                rightText = "Female Voice",
                isRightSelected = isFemaleVoice,
                onToggle = { viewModel.setVoice(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Slider
            var sliderPosition by remember(progress) { mutableStateOf(progress.toFloat()) }
            var isDragging by remember { mutableStateOf(false) }

            Slider(
                value = if (isDragging) sliderPosition else progress.toFloat(),
                onValueChange = { 
                    isDragging = true
                    sliderPosition = it 
                },
                onValueChangeFinished = {
                    isDragging = false
                    viewModel.seekTo(sliderPosition.toLong())
                },
                valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatTime(progress), style = MaterialTheme.typography.bodySmall)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .clickable {
                            val nextSpeed = when (playbackSpeed) {
                                0.75f -> 1.0f
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                2.0f -> 2.5f
                                2.5f -> 3.0f
                                else -> 0.75f
                            }
                            viewModel.setPlaybackSpeed(nextSpeed)
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(formatTime(duration), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = { viewModel.skipBackward() }) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Rewind 15s")
                }
                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(64.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else if (isPlaying) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(32.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(32.dp))
                    }
                }
                IconButton(onClick = { viewModel.skipForward() }) {
                    Icon(Icons.Default.FastForward, contentDescription = "Forward 15s")
                }
                IconButton(onClick = { viewModel.playNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
