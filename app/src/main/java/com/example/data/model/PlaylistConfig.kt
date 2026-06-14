package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistConfig(
    val book_title: String,
    val chapters: List<ChapterAudio>
)

@JsonClass(generateAdapter = true)
data class ChapterAudio(
    val id: String,
    val title: String,
    val type: String,
    val audio_url_male: String,
    val audio_url_female: String,
    val illustration_url: String,
    val explainer_url_male: String? = null,
    val explainer_url_female: String? = null
)
