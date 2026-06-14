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
import com.example.ui.screens.AudioScreen
import com.example.ui.screens.BookViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
            val isDarkMode by bookViewModel.isDarkMode.collectAsStateWithLifecycle()
            val darkTheme = isDarkMode ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = bookViewModel,
                            onReadClick = { navController.navigate("reader") },
                            onListenClick = { navController.navigate("audio") },
                            onPoemClick = { poemId -> navController.navigate("reader/$poemId") }
                        )
                    }
                    composable("reader") {
                        ReaderScreen(
                            viewModel = bookViewModel,
                            initialPoemId = null,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("reader/{poemId}") { backStackEntry ->
                        val poemId = backStackEntry.arguments?.getString("poemId")
                        ReaderScreen(
                            viewModel = bookViewModel,
                            initialPoemId = poemId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("audio") {
                        AudioScreen(
                            viewModel = bookViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
