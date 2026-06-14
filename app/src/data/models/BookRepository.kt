package com.example.data

import android.content.Context
import com.example.data.models.Book
import com.example.data.models.Chapter
import com.example.data.models.Poem
import com.example.data.room.AuthorEntity
import com.example.data.room.BookDao
import com.example.data.room.BookEntity
import com.example.data.room.PoemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject

class BookRepository(private val bookDao: BookDao, private val context: Context) {
    
    suspend fun initializeDatabase() {
        if (bookDao.getBooksCount() > 0) return // Already initialized

        // Read from assets
        val jsonString = context.assets.open("data/nadiya_teer.json").bufferedReader().use { it.readText() }
        val jsonData = JSONObject(jsonString)

        val authorData = jsonData.getJSONObject("author")
        val authorEntity = AuthorEntity(
            id = authorData.getString("id"),
            name = authorData.getString("name"),
            bio = authorData.getString("bio"),
            birthDate = authorData.getString("birthDate"),
            birthPlace = authorData.getString("birthPlace")
        )
        bookDao.insertAuthor(authorEntity)

        val bookData = jsonData.getJSONObject("book")
        val bookEntity = BookEntity(
            id = bookData.getString("id"),
            title = bookData.getString("title"),
            description = bookData.getString("description"),
            coverImageUrl = bookData.getString("coverImageUrl"),
            authorId = authorEntity.id
        )
        bookDao.insertBook(bookEntity)

        val chaptersData = jsonData.getJSONArray("chapters")
        val poemEntities = mutableListOf<PoemEntity>()
        for (cIndex in 0 until chaptersData.length()) {
            val c = chaptersData.getJSONObject(cIndex)
            val chapterId = c.getString("id")
            val chapterTitle = c.getString("title")
            val poemsData = c.getJSONArray("poems")
            
            for (i in 0 until poemsData.length()) {
                val p = poemsData.getJSONObject(i)
                poemEntities.add(
                    PoemEntity(
                        id = p.getString("id"),
                        bookId = bookEntity.id,
                        chapterId = chapterId,
                        chapterTitle = chapterTitle,
                        title = p.getString("title"),
                        stanzas = p.getJSONArray("stanzas").toString(), // Store as JSON string
                        sequenceOrder = p.getInt("sequenceOrder")
                    )
                )
            }
        }
        bookDao.insertPoems(poemEntities)
    }

    fun getBook(bookId: String): Flow<Book?> {
        return combine(
            bookDao.getBook(bookId),
            bookDao.getPoemsForBook(bookId)
        ) { bookEntity, poemEntities ->
            if (bookEntity == null) return@combine null

            val author = bookDao.getAuthor(bookEntity.authorId)
            
            // Group poems by Chapter
            val grouped = poemEntities.groupBy { it.chapterId }
            val mappedChapters = grouped.map { (chapId, poemsInChapter) ->
                val poems = poemsInChapter.map { pE ->
                    val stanzasArray = JSONArray(pE.stanzas)
                    val stanzasList = mutableListOf<String>()
                    for (i in 0 until stanzasArray.length()) {
                        stanzasList.add(stanzasArray.getString(i))
                    }
                    Poem(
                        id = pE.id,
                        title = pE.title,
                        stanzas = stanzasList
                    )
                }
                Chapter(
                    id = chapId,
                    title = poemsInChapter.firstOrNull()?.chapterTitle ?: "Poems",
                    poems = poems
                )
            }.sortedBy { it.id } // chap_1, chap_2, chap_3 etc.

            Book(
                id = bookEntity.id,
                title = bookEntity.title,
                author = author?.name ?: "Unknown",
                description = bookEntity.description,
                coverImageUrl = bookEntity.coverImageUrl,
                chapters = mappedChapters
            )
        }
    }
}
