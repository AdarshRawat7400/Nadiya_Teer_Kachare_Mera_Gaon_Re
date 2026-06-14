package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookRepository
import com.example.data.models.Book
import com.example.data.room.BookDao
import com.example.data.room.BookmarkEntity
import com.example.data.room.ReadingStateEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookViewModel(
    private val repository: BookRepository,
    private val dao: BookDao
) : ViewModel() {

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun toggleTheme(isDark: Boolean?) {
        _isDarkMode.value = isDark
    }

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    // Fixed book ID for this specific app
    private val bookId = "book_1"

    val currentBook: StateFlow<Book?> = repository.getBook(bookId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val bookMarks = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _readingState = MutableStateFlow<ReadingStateEntity?>(null)
    val readingState: StateFlow<ReadingStateEntity?> = _readingState.asStateFlow()

    init {
        loadStateForBook(bookId)
    }

    private fun loadStateForBook(bookId: String) {
        viewModelScope.launch {
            dao.getReadingState(bookId).collect { state ->
                 _readingState.value = state
            }
        }
    }

    fun saveProgress(bookId: String, chapterId: String, poemId: String) {
        viewModelScope.launch {
            val newState = ReadingStateEntity(bookId, chapterId, poemId)
            dao.saveReadingState(newState)
            _readingState.value = newState
        }
    }

    fun toggleBookmark(poemId: String) {
        viewModelScope.launch {
            val exists = bookMarks.value.any { it.poemId == poemId }
            if (exists) {
                dao.removeBookmark(poemId)
            } else {
                dao.insertBookmark(BookmarkEntity(poemId = poemId))
            }
        }
    }
}
