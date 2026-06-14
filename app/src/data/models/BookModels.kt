package com.example.data.models

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageUrl: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val id: String,
    val title: String,
    val poems: List<Poem>
)

data class Poem(
    val id: String,
    val title: String,
    val stanzas: List<String>
)
