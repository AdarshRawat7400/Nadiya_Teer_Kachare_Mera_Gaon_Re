package com.example.data.models

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageUrl: String,
    val chapters: List<Chapter>,
    val dictionary: List<DictionaryEntry> = emptyList()
)

data class Chapter(
    val id: String,
    val title: String,
    val poems: List<Poem>
)

data class Poem(
    val id: String,
    val title: String,
    val stanzas: List<String>,
    val pageNumber: String? = null,
    val explainer: String? = null
)

data class DictionaryEntry(
    val word: String,
    val meaning: String,
    val origin: String = "",
    val poem: String = "",
    val line: String = ""
)
