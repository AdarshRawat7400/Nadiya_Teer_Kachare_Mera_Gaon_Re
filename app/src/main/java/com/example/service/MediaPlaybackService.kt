package com.example.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.ForwardingPlayer
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.data.preference.AudiobookPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@UnstableApi
class MediaPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var preferences: AudiobookPreferences
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        preferences = AudiobookPreferences(applicationContext)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }

            override fun isCommandAvailable(command: @Player.Command Int): Boolean {
                if (command == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM || 
                    command == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM || 
                    command == Player.COMMAND_SEEK_TO_NEXT || 
                    command == Player.COMMAND_SEEK_TO_PREVIOUS) {
                    return true
                }
                return super.isCommandAvailable(command)
            }
            
            override fun hasNextMediaItem(): Boolean = true
            override fun hasPreviousMediaItem(): Boolean = true

            override fun seekToNextMediaItem() {
                applicationContext.sendBroadcast(Intent("com.example.ACTION_SKIP_NEXT"))
            }

            override fun seekToPreviousMediaItem() {
                applicationContext.sendBroadcast(Intent("com.example.ACTION_SKIP_PREVIOUS"))
            }
            
            override fun seekToNext() {
                seekToNextMediaItem()
            }
            
            override fun seekToPrevious() {
                seekToPreviousMediaItem()
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(pendingIntent)
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                saveCurrentState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (!isPlaying) {
                    saveCurrentState()
                }
            }
        })
    }

    private fun saveCurrentState() {
        val chapterId = player.currentMediaItem?.mediaId ?: return
        val position = player.currentPosition
        serviceScope.launch {
            preferences.savePlaybackState(chapterId, position)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        player.release()
        mediaSession?.release()
        super.onDestroy()
    }
}
