package com.example.data.repository

import android.content.Context
import com.example.data.model.PlaylistConfig
import com.example.data.model.ChapterAudio
import com.squareup.moshi.Moshi
import java.io.InputStreamReader

class AudiobookRepository(private val context: Context) {
    private var playlistConfig: PlaylistConfig? = null

    fun getPlaylistConfig(): PlaylistConfig {
        playlistConfig?.let { return it }

        val inputStream = context.assets.open("playlist_config.json")
        val reader = InputStreamReader(inputStream)
        val json = reader.readText()
        reader.close()

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(PlaylistConfig::class.java)
        
        val config = adapter.fromJson(json) ?: throw IllegalStateException("Failed to parse playlist config")
        playlistConfig = config
        return config
    }

    fun getChapter(id: String): ChapterAudio? {
        return getPlaylistConfig().chapters.find { it.id == id }
    }
}
