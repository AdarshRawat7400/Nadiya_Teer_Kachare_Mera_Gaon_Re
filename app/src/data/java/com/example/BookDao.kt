package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(author: AuthorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoems(poems: List<PoemEntity>)

    @Query("SELECT * FROM authors WHERE id = :id")
    suspend fun getAuthor(id: String): AuthorEntity?

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBook(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM poems WHERE bookId = :bookId ORDER BY sequenceOrder ASC")
    fun getPoemsForBook(bookId: String): Flow<List<PoemEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE poemId = :poemId")
    suspend fun removeBookmark(poemId: String)
    
    @Query("SELECT * FROM reading_state WHERE bookId = :bookId LIMIT 1")
    fun getReadingState(bookId: String): Flow<ReadingStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingState(state: ReadingStateEntity)
}
