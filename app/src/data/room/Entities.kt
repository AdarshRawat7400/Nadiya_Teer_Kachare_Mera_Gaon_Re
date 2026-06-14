package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bio: String,
    val birthDate: String,
    val birthPlace: String
)

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = AuthorEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("authorId")]
)
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverImageUrl: String,
    val authorId: String
)

@Entity(
    tableName = "poems",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class PoemEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val chapterId: String,
    val chapterTitle: String,
    val title: String,
    val stanzas: String, // Store as JSON string or delimited
    val sequenceOrder: Int
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val poemId: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_state")
data class ReadingStateEntity(
    @PrimaryKey val bookId: String,
    val lastReadChapterId: String,
    val lastReadPoemId: String,
    val timestamp: Long = System.currentTimeMillis()
)
