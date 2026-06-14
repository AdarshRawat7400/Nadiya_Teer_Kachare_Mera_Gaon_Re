package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.3f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (book != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Audio Cover Art
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book!!.coverImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Book Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .shadow(32.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = book!!.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "भाग 1: माटी की महक", // Placeholder for current chapter
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Progress Bar
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("02:15", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-14:30", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Media Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* speed */ }) {
                        Text("1.0x", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                    }
                    
                    IconButton(onClick = { progress = maxOf(0f, progress - 0.1f) }, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind", modifier = Modifier.size(32.dp))
                    }

                    FloatingActionButton(
                        onClick = { isPlaying = !isPlaying },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(onClick = { progress = minOf(1f, progress + 0.1f) }, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Forward", modifier = Modifier.size(32.dp))
                    }

                    IconButton(onClick = { /* sleep timer */ }) {
                        Text("🌙", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
