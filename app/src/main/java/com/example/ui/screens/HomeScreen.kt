package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthorSection
import com.example.ui.components.HeroSection
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.Scaffold
import com.example.ui.components.AppBottomNavigation

import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background

@Composable
fun HomeScreen(
    viewModel: BookViewModel,
    onReadClick: () -> Unit,
    onListenClick: () -> Unit,
    onVimochanClick: () -> Unit,
    onPoemClick: (String) -> Unit = {},
    onExplainerClick: (String) -> Unit = {}
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    
    val bookmarks by viewModel.bookMarks.collectAsStateWithLifecycle()
    val readingState by viewModel.readingState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("home") }
    var searchQuery by remember { mutableStateOf("") }

    if (book != null) {
        Scaffold(
            bottomBar = { 
                AppBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                ) 
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0.dp) // Avoid default scaffold insets, we handle top padding manually
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 32.dp
                )
            ) {
                if (currentTab == "home") {
                    item {
                        val context = LocalContext.current
                        val audioState by viewModel.audioState.collectAsStateWithLifecycle()
                        
                        HeroSection(
                            book = book!!,
                            themeMode = themeMode,
                            onToggleTheme = {
                                val next = when (themeMode) {
                                    com.example.ui.theme.AppThemeMode.SYSTEM -> com.example.ui.theme.AppThemeMode.LIGHT
                                    com.example.ui.theme.AppThemeMode.LIGHT -> com.example.ui.theme.AppThemeMode.DARK
                                    com.example.ui.theme.AppThemeMode.DARK -> com.example.ui.theme.AppThemeMode.SEPIA
                                    com.example.ui.theme.AppThemeMode.SEPIA -> com.example.ui.theme.AppThemeMode.SYSTEM
                                }
                                viewModel.setThemeMode(next) 
                            },
                            onReadClick = onReadClick,
                            onListenClick = {
                                val poemId = audioState?.lastPoemId ?: book!!.chapters.firstOrNull()?.poems?.firstOrNull()?.id
                                if (poemId != null) {
                                    val poem = book!!.chapters.flatMap { it.poems }.find { it.id == poemId }
                                    if (poem != null) {
                                        val audiobookViewModel = com.example.ui.screens.AudiobookProvider.getViewModel(context)
                                        val cleanTitle = poem.title.replace(Regex("^\\d+[_\\s-]*"), "")
                                        audiobookViewModel.playChapterByTitle(cleanTitle)
                                        onListenClick()
                                    }
                                }
                            },
                            onVimochanClick = onVimochanClick
                        )
                    }
                    
                    item {
                        val context = LocalContext.current
                        val audiobookViewModel = com.example.ui.screens.AudiobookProvider.getViewModel(context)
                        com.example.ui.components.NowPlayingMiniPlayer(
                            viewModel = audiobookViewModel,
                            onNavigateToPlayer = onListenClick,
                            onClose = { audiobookViewModel.closePlayer() }
                        )
                    }
                    
                    item {
                        val context = LocalContext.current
                        val audioState by viewModel.audioState.collectAsStateWithLifecycle()
                        if (audioState != null) {
                            val poem = book!!.chapters.flatMap { it.poems }.find { it.id == audioState!!.lastPoemId }
                            val chapter = book!!.chapters.find { it.poems.any { p -> p.id == audioState!!.lastPoemId } }
                            if (poem != null) {
                                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Continue Listening",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    androidx.compose.material3.ElevatedCard(
                                        modifier = Modifier.fillMaxWidth().clickable { 
                                            val audiobookViewModel = com.example.ui.screens.AudiobookProvider.getViewModel(context)
                                            val cleanTitle = poem.title.replace(Regex("^\\d+[_\\s-]*"), "")
                                            audiobookViewModel.playChapterByTitle(cleanTitle, startPosition = audioState!!.positionMillis)
                                            onListenClick()
                                        },
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Resume",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Chapter: ${chapter?.title?.replace(Regex("^\\d+[_\\s-]*"), "") ?: "Part 1"}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = poem.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                // Simplified timestamp presentation
                                                Text(
                                                    text = "Resume from ${String.format("%02d:%02d", (audioState!!.positionMillis / 1000) / 60, (audioState!!.positionMillis / 1000) % 60)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "About this Book",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = book!!.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        AuthorSection(book = book!!)
                    }
                } else if (currentTab == "chapters") {
                    item(key = "chapters_header") {
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "Table of Contents",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            androidx.compose.material3.OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search poems...") },
                                leadingIcon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search icon"
                                    )
                                },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    item(key = "chapters_list") {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                book!!.chapters.forEachIndexed { index, chapter ->
                                    val filteredPoems = chapter.poems.filter { 
                                        it.title.contains(searchQuery, ignoreCase = true) || 
                                        it.stanzas.any { s -> s.contains(searchQuery, ignoreCase = true) }
                                    }
                                    if (filteredPoems.isNotEmpty()) {
                                        androidx.compose.material3.ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Text(
                                                    text = chapter.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(bottom = 12.dp)
                                                )
                                                
                                                filteredPoems.forEach { poem ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { onPoemClick(poem.id) }
                                                            .padding(vertical = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .background(
                                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        
                                                        Text(
                                                            text = poem.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        
                                                        if (poem.explainer != null) {
                                                            com.example.ui.components.TooltipIconButton(
                                                                tooltipText = "भावार्थ",
                                                                onClick = { onExplainerClick(poem.id) },
                                                                modifier = Modifier.padding(end = 4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.Lightbulb,
                                                                    contentDescription = "Explainer Available",
                                                                    tint = MaterialTheme.colorScheme.secondary
                                                                )
                                                            }
                                                        }
                                                        
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                            contentDescription = "Read",
                                                            tint = MaterialTheme.colorScheme.outline
                                                        )
                                                    }
                                                    if (poem != filteredPoems.last()) {
                                                        HorizontalDivider(
                                                            modifier = Modifier.padding(start = 48.dp),
                                                            color = MaterialTheme.colorScheme.surfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (currentTab == "saved") {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "Saved Bookmarks",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val bookmarkedPoems = book!!.chapters.flatMap { it.poems }.filter { poem -> bookmarks.any { it.poemId == poem.id } }
                            if (bookmarkedPoems.isEmpty()) {
                                Text(
                                    text = "No poems bookmarked yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                androidx.compose.foundation.layout.Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    bookmarkedPoems.forEach { poem ->
                                        androidx.compose.material3.ElevatedCard(
                                            modifier = Modifier.fillMaxWidth().clickable { onPoemClick(poem.id) },
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Bookmark,
                                                    contentDescription = "Bookmark",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = poem.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (currentTab == "recent") {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "Recent Reading",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val history by viewModel.readingHistory.collectAsStateWithLifecycle()
                            
                            val recentPoems = history.distinctBy { it.poemId }
                                .take(5)
                                .mapNotNull { he -> book!!.chapters.flatMap { it.poems }.find { it.id == he.poemId } }

                            if (recentPoems.isEmpty()) {
                                Text(
                                    text = "No recent reading activity.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                androidx.compose.foundation.layout.Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    recentPoems.forEachIndexed { index, recentPoem ->
                                        androidx.compose.material3.ElevatedCard(
                                            modifier = Modifier.fillMaxWidth().clickable { onPoemClick(recentPoem.id) },
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    if (index == 0) {
                                                        Text(
                                                            text = "Last read",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(
                                                        text = recentPoem.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                if (recentPoem.explainer != null) {
                                                    com.example.ui.components.TooltipIconButton(
                                                        tooltipText = "भावार्थ",
                                                        onClick = { onExplainerClick(recentPoem.id) },
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Lightbulb,
                                                            contentDescription = "Explainer Available",
                                                            tint = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = "Read",
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            
            item {
                Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
            }
        } // End LazyColumn
        } // End Scaffold scope
    } else {
        // Loading State
        Box(modifier = Modifier.fillMaxSize())
    }
}
