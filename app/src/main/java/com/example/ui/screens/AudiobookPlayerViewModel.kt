package com.example.ui.screens

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.model.ChapterAudio
import com.example.data.preference.AudiobookPreferences
import com.example.data.repository.AudiobookRepository
import com.example.service.MediaPlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AudiobookPlayerViewModel(
    private val context: Context,
    private val repository: AudiobookRepository,
    private val preferences: AudiobookPreferences
) : ViewModel() {

    private var _mediaController: MediaController? = null
    
    private val _isPlayerReady = MutableStateFlow(false)
    val isPlayerReady = _isPlayerReady.asStateFlow()

    private val _currentChapter = MutableStateFlow<ChapterAudio?>(null)
    val currentChapter = _currentChapter.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isFemaleVoice = MutableStateFlow(false)
    val isFemaleVoice = _isFemaleVoice.asStateFlow()

    private val _isExplainerActive = MutableStateFlow(false)
    val isExplainerActive = _isExplainerActive.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private var isUpdatingProgress = true

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.ACTION_SKIP_NEXT" -> playNext()
                "com.example.ACTION_SKIP_PREVIOUS" -> playPrevious()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction("com.example.ACTION_SKIP_NEXT")
            addAction("com.example.ACTION_SKIP_PREVIOUS")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(actionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(actionReceiver, filter)
        }
        initializeController()
        startProgressUpdate()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MediaPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            _mediaController = controllerFuture.get()
            _isPlayerReady.value = true
            setupPlayerListener()
            
            val pending = pendingPlayRequest
            if (pending != null) {
                pendingPlayRequest = null
                playChapter(pending.chapterId, pending.startPosition, pending.playWhenReady, pending.forceExplainer)
            } else {
                // Try resolving previous state automatically
                viewModelScope.launch {
                    val state = preferences.playbackStateFlow.first()
                    if (state.chapterId != null && _mediaController?.playbackState != Player.STATE_READY) {
                        val chapter = repository.getChapter(state.chapterId)
                        if (chapter != null) {
                            _currentChapter.value = chapter
                            playChapter(chapter.id, state.position, playWhenReady = false)
                        }
                    }
                }
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        _mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                _isLoading.value = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    _duration.value = _mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.let { id ->
                    _currentChapter.value = repository.getChapter(id)
                }
            }
        })
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (isUpdatingProgress) {
                val ctrl = _mediaController
                if (ctrl != null && ctrl.isPlaying) {
                    _progress.value = ctrl.currentPosition.coerceAtLeast(0L)
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun playDefaultIfEmpty() {
        if (_currentChapter.value == null) {
            val config = repository.getPlaylistConfig()
            if (config.chapters.isNotEmpty()) {
                playChapter(config.chapters.first().id, playWhenReady = false)
            }
        }
    }

    fun playChapterByTitle(title: String, startPosition: Long = 0L, playWhenReady: Boolean = true, forceExplainer: Boolean? = null) {
        val chapter = repository.getPlaylistConfig().chapters.find {
            it.title.replace(Regex("^\\d+[_\\s-]*"), "") == title
        } ?: return
        playChapter(chapter.id, startPosition, playWhenReady, forceExplainer)
    }

    private var pendingPlayRequest: PendingPlayRequest? = null
    private data class PendingPlayRequest(val chapterId: String, val startPosition: Long, val playWhenReady: Boolean, val forceExplainer: Boolean?)

    fun playChapter(chapterId: String, startPosition: Long = 0L, playWhenReady: Boolean = true, forceExplainer: Boolean? = null) {
        val chapter = repository.getChapter(chapterId) ?: return
        _currentChapter.value = chapter
        if (forceExplainer != null) {
            _isExplainerActive.value = forceExplainer && chapter.type == "poem"
        }
        
        if (_mediaController == null) {
            pendingPlayRequest = PendingPlayRequest(chapterId, startPosition, playWhenReady, forceExplainer)
            return
        }

        val url = getActiveUrl(chapter, _isFemaleVoice.value, _isExplainerActive.value)
        val mediaItem = createMediaItem(chapter, url)
        
        _mediaController?.let {
            it.setMediaItem(mediaItem, startPosition)
            it.setPlaybackSpeed(_playbackSpeed.value)
            it.prepare()
            it.playWhenReady = playWhenReady
        }
    }

    fun togglePlayPause() {
        _mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekTo(position: Long) {
        _mediaController?.seekTo(position)
        _progress.value = position
    }

    fun skipForward() = seekTo((_mediaController?.currentPosition ?: 0L) + 15_000L)
    fun skipBackward() = seekTo((_mediaController?.currentPosition ?: 0L) - 15_000L)

    fun playNext() {
        val currentId = _currentChapter.value?.id ?: return
        val allChapters = repository.getPlaylistConfig().chapters
        val idx = allChapters.indexOfFirst { it.id == currentId }
        if (idx != -1 && idx + 1 < allChapters.size) {
            playChapter(allChapters[idx + 1].id)
        }
    }

    fun playPrevious() {
        val currentId = _currentChapter.value?.id ?: return
        val allChapters = repository.getPlaylistConfig().chapters
        val idx = allChapters.indexOfFirst { it.id == currentId }
        if (idx > 0) {
            playChapter(allChapters[idx - 1].id)
        }
    }

    fun setVoice(isFemale: Boolean) {
        if (_isFemaleVoice.value == isFemale) return
        _isFemaleVoice.value = isFemale
        reapplySeamless()
    }

    fun setExplainer(isExplainer: Boolean) {
        val type = _currentChapter.value?.type
        if (type != "poem") return // Only available for poem
        if (_isExplainerActive.value == isExplainer) return
        _isExplainerActive.value = isExplainer
        reapplySeamless()
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        _mediaController?.setPlaybackSpeed(speed)
    }

    private fun reapplySeamless() {
        val chapter = _currentChapter.value ?: return
        val position = _mediaController?.currentPosition ?: 0L
        val wasPlaying = _mediaController?.isPlaying ?: false
        val url = getActiveUrl(chapter, _isFemaleVoice.value, _isExplainerActive.value)
        val mediaItem = createMediaItem(chapter, url)
        
        _mediaController?.let {
            it.setMediaItem(mediaItem, position)
            it.setPlaybackSpeed(_playbackSpeed.value)
            it.prepare()
            if (wasPlaying) it.play()
        }
    }

    private fun getActiveUrl(chapter: ChapterAudio, isFemale: Boolean, isExplainer: Boolean): String {
        return if (isExplainer && chapter.type == "poem") {
            if (isFemale) chapter.explainer_url_female ?: chapter.explainer_url_male ?: ""
            else chapter.explainer_url_male ?: ""
        } else {
            if (isFemale) chapter.audio_url_female
            else chapter.audio_url_male
        }
    }

    private fun createMediaItem(chapter: ChapterAudio, url: String): MediaItem {
        val cleanTitle = chapter.title.replace(Regex("^\\d+[_\\s-]*"), "")
        val metadata = MediaMetadata.Builder()
            .setTitle(cleanTitle)
            .setArtworkUri(android.net.Uri.parse(chapter.illustration_url))
            .build()
        return MediaItem.Builder()
            .setMediaId(chapter.id)
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()
    }

    fun closePlayer() {
        _mediaController?.pause()
        _currentChapter.value = null
    }

    override fun onCleared() {
        isUpdatingProgress = false
        _mediaController?.release()
        try {
            context.unregisterReceiver(actionReceiver)
        } catch (e: Exception) {}
        super.onCleared()
    }
}
