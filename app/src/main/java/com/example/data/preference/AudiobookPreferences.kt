package com.example.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "audiobook_prefs")

class AudiobookPreferences(private val context: Context) {
    companion object {
        val CHAPTER_ID_KEY = stringPreferencesKey("chapter_id")
        val CURRENT_POSITION_KEY = longPreferencesKey("current_position")
    }

    val playbackStateFlow: Flow<PlaybackState> = context.dataStore.data.map { preferences ->
        PlaybackState(
            chapterId = preferences[CHAPTER_ID_KEY],
            position = preferences[CURRENT_POSITION_KEY] ?: 0L
        )
    }

    suspend fun savePlaybackState(chapterId: String, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[CHAPTER_ID_KEY] = chapterId
            preferences[CURRENT_POSITION_KEY] = position
        }
    }
}

data class PlaybackState(
    val chapterId: String?,
    val position: Long
)
