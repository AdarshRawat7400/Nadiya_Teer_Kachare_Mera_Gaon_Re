package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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

import androidx.compose.material3.Scaffold
import com.example.ui.components.AppBottomNavigation

import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background

@Composable
fun HomeScreen(
    viewModel: BookViewModel,
    onReadClick: () -> Unit,
    onListenClick: () -> Unit,
    onPoemClick: (String) -> Unit = {}
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    val isDarkModeState by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isDarkMode = isDarkModeState ?: isSystemInDarkTheme()

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
                        HeroSection(
                            book = book!!,
                            isDarkMode = isDarkMode,
                            onToggleTheme = { viewModel.toggleTheme(!isDarkMode) },
                            onReadClick = onReadClick,
                            onListenClick = onListenClick
                        )
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
                    item {
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
                            
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                book!!.chapters.forEachIndexed { index, chapter ->
                                    val filteredPoems = chapter.poems.filter { 
                                        it.title.contains(searchQuery, ignoreCase = true) 
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
                                                    text = chapter.title,
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
                                                            text = poem.title,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        
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
                            Text(
                                text = "No poems bookmarked yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                            Text(
                                text = "No recent reading activity.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
