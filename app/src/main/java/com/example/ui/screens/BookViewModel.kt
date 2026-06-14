package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookRepository
import com.example.data.models.Book
import com.example.data.room.BookDao
import com.example.data.room.BookmarkEntity
import com.example.data.room.ReadingStateEntity
import com.example.data.room.AudioStateEntity
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

    private val _themeMode = MutableStateFlow(com.example.ui.theme.AppThemeMode.SYSTEM)
    val themeMode: StateFlow<com.example.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        _themeMode.value = mode
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

    private val _audioState = MutableStateFlow<AudioStateEntity?>(null)
    val audioState: StateFlow<AudioStateEntity?> = _audioState.asStateFlow()

    init {
        loadStateForBook(bookId)
    }

    private fun loadStateForBook(bookId: String) {
        viewModelScope.launch {
            dao.getReadingState(bookId).collect { state ->
                 _readingState.value = state
            }
        }
        viewModelScope.launch {
            dao.getAudioState(bookId).collect { state ->
                 _audioState.value = state
            }
        }
    }

    val readingHistory = dao.getReadingHistory(bookId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveProgress(bookId: String, chapterId: String, poemId: String) {
        viewModelScope.launch {
            val newState = ReadingStateEntity(bookId, chapterId, poemId)
            dao.saveReadingState(newState)
            _readingState.value = newState
            
            // Also add to history
            val existing = readingHistory.value.find { it.poemId == poemId }
            if (existing == null) {
                dao.addReadingHistory(com.example.data.room.ReadingHistoryEntity(bookId = bookId, poemId = poemId))
            } else {
                // To bump it to top we would be updating timestamp but let's just insert a new one if we want full log, or delete and reinsert.
                // Insert replaces if same PK, but here PK is auto generated. We can let history grow.
                dao.addReadingHistory(com.example.data.room.ReadingHistoryEntity(bookId = bookId, poemId = poemId))
            }
        }
    }

    fun saveAudioState(bookId: String, poemId: String, position: Long, speed: Float) {
        viewModelScope.launch {
            val newState = com.example.data.room.AudioStateEntity(bookId, poemId, position, speed)
            dao.saveAudioState(newState)
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
