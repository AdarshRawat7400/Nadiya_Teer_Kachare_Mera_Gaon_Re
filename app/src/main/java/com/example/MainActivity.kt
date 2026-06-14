package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.BookRepository
import com.example.data.room.AppDatabase
import com.example.ui.screens.BookViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background

import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { BookRepository(database.bookDao(), context) }
            
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return BookViewModel(repository, database.bookDao()) as T
                }
            }
            
            val bookViewModel: BookViewModel = viewModel(factory = factory)
            val themeMode by bookViewModel.themeMode.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                val audiobookViewModel = com.example.ui.screens.AudiobookProvider.getViewModel(context)
                val showPlayerScreen by com.example.ui.screens.AudiobookProvider.showPlayerScreen.collectAsStateWithLifecycle()

                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                            NavHost(navController = navController, startDestination = "home") {
                                composable("home") {
                                    HomeScreen(
                                        viewModel = bookViewModel,
                                        onReadClick = { navController.navigate("reader") },
                                        // Update onListenClick to open our new audio player
                                        onListenClick = { com.example.ui.screens.AudiobookProvider.openPlayer(context) },
                                        onVimochanClick = { navController.navigate("vimochan") },
                                        onPoemClick = { poemId -> navController.navigate("reader/$poemId") },
                                        onExplainerClick = { poemId -> navController.navigate("reader/$poemId?showExplainer=true") }
                                    )
                                }
                                composable("reader") {
                                    ReaderScreen(
                                        viewModel = bookViewModel,
                                        initialPoemId = null,
                                        showExplainerInitial = false,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable("reader/{poemId}?showExplainer={showExplainer}") { backStackEntry ->
                                    val poemId = backStackEntry.arguments?.getString("poemId")
                                    val showExplainer = backStackEntry.arguments?.getString("showExplainer")?.toBoolean() ?: false
                                    ReaderScreen(
                                        viewModel = bookViewModel,
                                        initialPoemId = poemId,
                                        showExplainerInitial = showExplainer,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable("audio") {
                                    // Fallback if existing code navigates here
                                    com.example.ui.screens.AudiobookPlayerScreen(
                                        viewModel = audiobookViewModel,
                                        bookViewModel = bookViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable("vimochan") {
                                    com.example.ui.screens.VimochanScreen(
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }

                        // Mini player removed to use local ones and keep bottom nav at bottom
                    }

                    // Full screen player overlay
                    if (showPlayerScreen) {
                        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                            com.example.ui.screens.AudiobookPlayerScreen(
                                viewModel = audiobookViewModel,
                                bookViewModel = bookViewModel,
                                onBack = { com.example.ui.screens.AudiobookProvider.closePlayer() }
                            )
                        }
                    }
                }
            }
        }
    }
}
