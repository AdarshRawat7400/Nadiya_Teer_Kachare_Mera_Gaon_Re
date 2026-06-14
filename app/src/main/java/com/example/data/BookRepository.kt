package com.example.data

import android.content.Context
import com.example.data.models.Book
import com.example.data.models.Chapter
import com.example.data.models.DictionaryEntry
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

        val bookData = jsonData.getJSONObject("book")
        val bookEntity = BookEntity(
            id = bookData.getString("id"),
            title = bookData.getString("title"),
            description = bookData.getString("description"),
            coverImageUrl = bookData.getString("coverImageUrl"),
            authorId = authorEntity.id,
            dictionary = if (jsonData.has("dictionary") && !jsonData.isNull("dictionary")) {
                jsonData.getJSONArray("dictionary").toString()
            } else null
        )

        val poemEntities = mutableListOf<PoemEntity>()
        
        try {
            if (jsonData.has("chapters")) {
                val chaptersData = jsonData.getJSONArray("chapters")
                for (cIndex in 0 until chaptersData.length()) {
                    val c = chaptersData.getJSONObject(cIndex)
                    val chapterId = c.optString("id", "chapter_$cIndex")
                    val chapterTitle = c.optString("title", "Chapter")
                    if (c.has("poems")) {
                        val poemsData = c.getJSONArray("poems")
                        
                        for (i in 0 until poemsData.length()) {
                            val p = poemsData.getJSONObject(i)
                            poemEntities.add(
                                PoemEntity(
                                    id = p.optString("id", "poem_$i"),
                                    bookId = bookEntity.id,
                                    chapterId = chapterId,
                                    chapterTitle = chapterTitle,
                                    title = p.optString("title", "Untitled"),
                                    stanzas = p.optJSONArray("stanzas")?.toString() ?: "[]",
                                    sequenceOrder = p.optInt("sequenceOrder", i),
                                    pageNumber = if (p.has("pageNumber") && !p.isNull("pageNumber")) p.getString("pageNumber") else null,
                                    explainer = if (p.has("explainer") && !p.isNull("explainer")) p.getString("explainer") else null
                                )
                            )
                        }
                    }
                }
            } else if (jsonData.has("poems")) {
                val poemsData = jsonData.getJSONArray("poems")
                for (i in 0 until poemsData.length()) {
                    val p = poemsData.getJSONObject(i)
                    poemEntities.add(
                        PoemEntity(
                            id = p.optString("id", "poem_$i"),
                            bookId = bookEntity.id,
                            chapterId = "chapter_1",
                            chapterTitle = "कविताएँ",
                            title = p.optString("title", "Untitled"),
                            stanzas = p.optJSONArray("stanzas")?.toString() ?: "[]",
                            sequenceOrder = p.optInt("sequenceOrder", i),
                            pageNumber = if (p.has("pageNumber") && !p.isNull("pageNumber")) p.getString("pageNumber") else null,
                            explainer = if (p.has("explainer") && !p.isNull("explainer")) p.getString("explainer") else null
                        )
                    )
                }
            }
            
            bookDao.insertAuthor(authorEntity)
            bookDao.insertBook(bookEntity)
            bookDao.insertPoems(poemEntities)
        } catch (e: Exception) {
            e.printStackTrace()
            // Ensure partial data isn't left if we can't parse
        }
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
                        stanzas = stanzasList,
                        pageNumber = pE.pageNumber,
                        explainer = pE.explainer
                    )
                }
                Chapter(
                    id = chapId,
                    title = poemsInChapter.firstOrNull()?.chapterTitle ?: "Poems",
                    poems = poems
                )
            }.sortedBy { it.id } // chap_1, chap_2, chap_3 etc.

            val dictionaryList = mutableListOf<DictionaryEntry>()
            if (bookEntity.dictionary != null && bookEntity.dictionary.isNotEmpty() && bookEntity.dictionary.startsWith("[")) {
                try {
                    val arr = JSONArray(bookEntity.dictionary)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        dictionaryList.add(DictionaryEntry(
                            word = obj.optString("word", ""),
                            meaning = obj.optString("meaning", ""),
                            origin = obj.optString("origin", ""),
                            poem = obj.optString("poem", ""),
                            line = obj.optString("line", "")
                        ))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Book(
                id = bookEntity.id,
                title = bookEntity.title,
                author = author?.name ?: "Unknown",
                description = bookEntity.description,
                coverImageUrl = bookEntity.coverImageUrl,
                chapters = mappedChapters,
                dictionary = dictionaryList
            )
        }
    }
}
