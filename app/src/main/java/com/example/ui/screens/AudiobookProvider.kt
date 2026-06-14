package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.preference.AudiobookPreferences
import com.example.data.repository.AudiobookRepository
import kotlinx.coroutines.flow.MutableStateFlow

object AudiobookProvider {
    private var instance: AudiobookPlayerViewModel? = null
    
    val showPlayerScreen = MutableStateFlow(false)

    fun getViewModel(context: Context): AudiobookPlayerViewModel {
        if (instance == null) {
            val appCtx = context.applicationContext
            val repo = AudiobookRepository(appCtx)
            val prefs = AudiobookPreferences(appCtx)
            instance = AudiobookPlayerViewModel(appCtx, repo, prefs)
        }
        return instance!!
    }

    fun openPlayer(context: Context) {
        showPlayerScreen.value = true
        getViewModel(context).playDefaultIfEmpty()
    }

    fun closePlayer() {
        showPlayerScreen.value = false
    }
}
