package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BookViewModel,
    initialPoemId: String? = null,
    onNavigateBack: () -> Unit
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookMarks.collectAsStateWithLifecycle()

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
            val listState = rememberLazyListState()
            
            LaunchedEffect(initialPoemId, book) {
                if (initialPoemId != null && book != null) {
                    var targetIndex = 1 // 1 for the book title item
                    var found = false
                    for (chapter in book!!.chapters) {
                        targetIndex++ // 1 for chapter header
                        for (poem in chapter.poems) {
                            if (poem.id == initialPoemId) {
                                found = true
                                break
                            }
                            targetIndex++
                        }
                        if (found) break
                    }
                    if (found) {
                        listState.scrollToItem(targetIndex)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Book Title
                item {
                    Text(
                        text = book!!.title,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp)
                    )
                }

                book!!.chapters.forEach { chapter ->
                    item {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 48.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    items(chapter.poems) { poem ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = poem.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            poem.stanzas.forEach { stanza ->
                                com.example.ui.components.MarkdownText(
                                    text = stanza,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
