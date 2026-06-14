package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import com.example.data.models.DictionaryEntry

sealed class ReaderItem {
    data class TitleItem(val title: String) : ReaderItem()
    data class ChapterItem(val chapter: com.example.data.models.Chapter) : ReaderItem()
    data class PoemItem(val chapter: com.example.data.models.Chapter, val poem: com.example.data.models.Poem) : ReaderItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BookViewModel,
    initialPoemId: String? = null,
    showExplainerInitial: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookMarks.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    var activeExplainerPoemIds by remember(initialPoemId, showExplainerInitial) { 
        mutableStateOf(if (showExplainerInitial && initialPoemId != null) setOf(initialPoemId) else emptySet()) 
    }

    val readerItems = remember(book) {
        val list = mutableListOf<ReaderItem>()
        if (book != null) {
            list.add(ReaderItem.TitleItem(book!!.title))
            book!!.chapters.forEach { chapter ->
                list.add(ReaderItem.ChapterItem(chapter))
                chapter.poems.forEach { poem ->
                    list.add(ReaderItem.PoemItem(chapter, poem))
                }
            }
        }
        list
    }

    val pagerState = rememberPagerState(pageCount = { readerItems.size.coerceAtLeast(1) })

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    com.example.ui.components.TooltipIconButton(
                        tooltipText = "पीछे",
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                    com.example.ui.components.TooltipIconButton(
                        tooltipText = "Play Audio",
                        onClick = {
                            val ctrl = com.example.ui.screens.AudiobookProvider.getViewModel(context)
                            val item = readerItems.getOrNull(pagerState.currentPage)
                            if (item is ReaderItem.PoemItem) {
                                val isExplainer = activeExplainerPoemIds.contains(item.poem.id)
                                ctrl.playChapterByTitle(item.poem.title, playWhenReady = true, forceExplainer = isExplainer)
                            } else if (item is ReaderItem.ChapterItem) {
                                ctrl.playChapterByTitle(item.chapter.title)
                            }
                            com.example.ui.screens.AudiobookProvider.openPlayer(context)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Audio"
                        )
                    }
                    com.example.ui.components.TooltipIconButton(
                        tooltipText = "थीम बदलें",
                        onClick = { 
                        val next = when (themeMode) {
                            com.example.ui.theme.AppThemeMode.SYSTEM -> com.example.ui.theme.AppThemeMode.LIGHT
                            com.example.ui.theme.AppThemeMode.LIGHT -> com.example.ui.theme.AppThemeMode.DARK
                            com.example.ui.theme.AppThemeMode.DARK -> com.example.ui.theme.AppThemeMode.SEPIA
                            com.example.ui.theme.AppThemeMode.SEPIA -> com.example.ui.theme.AppThemeMode.SYSTEM
                        }
                        viewModel.setThemeMode(next) 
                    }) {
                        val icon = when (themeMode) {
                            com.example.ui.theme.AppThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness
                            com.example.ui.theme.AppThemeMode.LIGHT -> Icons.Filled.LightMode
                            com.example.ui.theme.AppThemeMode.DARK -> Icons.Filled.DarkMode
                            com.example.ui.theme.AppThemeMode.SEPIA -> Icons.Filled.Palette
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle Theme"
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
            var showDictionaryBottomSheet by remember { mutableStateOf(false) }
            var currentPageNumber by remember { mutableStateOf<String?>(null) }

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(initialPoemId, book) {
                if (initialPoemId != null && book != null) {
                    val index = readerItems.indexOfFirst { it is ReaderItem.PoemItem && it.poem.id == initialPoemId }
                    if (index != -1) {
                        pagerState.scrollToPage(index)
                    }
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                val item = readerItems[pagerState.currentPage]
                if (item is ReaderItem.PoemItem) {
                    viewModel.saveProgress(book!!.id, item.chapter.id, item.poem.id)
                    currentPageNumber = item.poem.pageNumber
                } else {
                    currentPageNumber = null
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background)
                    ) { page ->
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                when (val item = readerItems[page]) {
                                is ReaderItem.TitleItem -> {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = item.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                            style = MaterialTheme.typography.displayLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
                                        )
                                    }
                                }
                                is ReaderItem.ChapterItem -> {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = item.chapter.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 48.dp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                                is ReaderItem.PoemItem -> {
                                    val chapter = item.chapter
                                    val poem = item.poem

                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (chapter.id == "chapter_3_author" || poem.title.replace(Regex("^\\d+[_\\s-]*"), "") == "रचनाकार परिचय") {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.author_pic),
                                                contentDescription = "Author Photo",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 24.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isBookmarked = bookmarks.any { it.poemId == poem.id }
                                            com.example.ui.components.TooltipIconButton(
                                                tooltipText = if (isBookmarked) "बुकमार्क हटाएं" else "बुकमार्क करें",
                                                onClick = { viewModel.toggleBookmark(poem.id) }
                                            ) {
                                                Icon(
                                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            
                                            if (poem.explainer != null) {
                                                val isShowingExplainer = activeExplainerPoemIds.contains(poem.id)
                                                com.example.ui.components.TooltipIconButton(
                                                    tooltipText = "भावार्थ",
                                                    onClick = { 
                                                        activeExplainerPoemIds = if (isShowingExplainer) {
                                                            activeExplainerPoemIds - poem.id
                                                        } else {
                                                            activeExplainerPoemIds + poem.id
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isShowingExplainer) Icons.Default.Lightbulb else Icons.Outlined.Lightbulb,
                                                        contentDescription = "Toggle Explainer",
                                                        tint = if (isShowingExplainer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                            
                                            if (book!!.dictionary.isNotEmpty()) {
                                                com.example.ui.components.TooltipIconButton(
                                                    tooltipText = "शब्दकोश",
                                                    onClick = {
                                                        showDictionaryBottomSheet = true
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                        contentDescription = "Dictionary",
                                                        tint = MaterialTheme.colorScheme.onBackground
                                                    )
                                                }
                                            }
                                            
                                            Text(
                                                text = poem.title.replace(Regex("^\\d+[_\\s-]*"), ""),
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                            // Placeholder for balance
                                            Spacer(modifier = Modifier.width(48.dp))
                                        }

                                        val isShowingExplainer = activeExplainerPoemIds.contains(poem.id)
                                        
                                        if (isShowingExplainer && poem.explainer != null) {
                                            Text(
                                                text = "भावार्थ",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                                            )
                                            com.example.ui.components.MarkdownText(
                                                text = poem.explainer,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth()
                                            )
                                        } else {
                                            poem.stanzas.forEach { stanza ->
                                                val isList = stanza.lines().any { it.trim().startsWith("♦") || it.trim().startsWith("- ") || it.trim().startsWith("* ") }
                                                com.example.ui.components.MarkdownText(
                                                    text = stanza,
                                                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    textAlign = if (isList) TextAlign.Start else TextAlign.Center,
                                                    modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth()
                                                )
                                            }
                                        }
                                        
                                        if (poem.pageNumber != null) {
                                            Text(
                                                text = "- ${poem.pageNumber} -",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }

                    // Bottom Navigation Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { 
                                    coroutineScope.launch {
                                        if (pagerState.currentPage > 0) {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage > 0
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("पिछला") // Previous
                            }

                            Text(
                                text = "${pagerState.currentPage + 1} / ${readerItems.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(
                                onClick = { 
                                    coroutineScope.launch {
                                        if (pagerState.currentPage < readerItems.size - 1) {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage < readerItems.size - 1
                            ) {
                                Text("अगला") // Next
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, modifier = Modifier.rotate(180f), contentDescription = "Next")
                            }
                        }
                    }
                }
            }

            // Dictionary Bottom Sheet
            if (showDictionaryBottomSheet && book!!.dictionary.isNotEmpty()) {
                ModalBottomSheet(
                    onDismissRequest = { showDictionaryBottomSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredDictionary = remember(searchQuery, book!!.dictionary) {
                        if (searchQuery.isBlank()) {
                            book!!.dictionary
                        } else {
                            book!!.dictionary.filter { 
                                it.word.contains(searchQuery, ignoreCase = true) || 
                                it.meaning.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "शब्दकोश",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("खोजें...") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    com.example.ui.components.TooltipIconButton(
                                        tooltipText = "साफ़ करें",
                                        onClick = { searchQuery = "" }
                                    ) {
                                        Icon(Icons.Default.Clear, "Clear")
                                    }
                                } else {
                                    Icon(Icons.Default.Search, "Search")
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (filteredDictionary.isEmpty()) {
                            Text(
                                "No words found.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(filteredDictionary) { entry ->
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Text(text = entry.word, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        Text(text = entry.meaning, style = MaterialTheme.typography.bodyLarge)
                                        if (entry.origin.isNotBlank()) {
                                            Text(text = "मूल: ${entry.origin}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (entry.poem.isNotBlank() || entry.line.isNotBlank()) {
                                            val sourceStr = listOf(entry.poem, entry.line.let { if (it.isNotBlank()) "\"$it\"" else "" }).filter { it.isNotBlank() }.joinToString(" - ")
                                            Text(text = sourceStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}