package com.example.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

tailrec fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Composable
fun VimochanScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.getActivity()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Set a default CookieManager so ExoPlayer can handle Google Drive cookies + redirects
    remember {
        if (CookieHandler.getDefault() == null) {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            CookieHandler.setDefault(cookieManager)
        }
    }

    val videoUrl = "https://drive.google.com/uc?export=download&id=1KBJHD7SvqU9Kpg-WfyKdpvnCDm227sYq"

    var playerError by remember { mutableStateOf<androidx.media3.common.PlaybackException?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
            .apply {
                val baseDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    
                val resolver = androidx.media3.datasource.ResolvingDataSource.Resolver { dataSpec ->
                    val urlString = dataSpec.uri.toString()
                    if (urlString.contains("drive.google.com")) {
                        try {
                            val url = java.net.URL(urlString)
                            val connection = url.openConnection() as java.net.HttpURLConnection
                            connection.instanceFollowRedirects = true
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                            connection.connect()
                            
                            val contentType = connection.contentType ?: ""
                            if (contentType.startsWith("text/html")) {
                                val html = connection.inputStream.bufferedReader().use { it.readText() }
                                connection.disconnect()
                                
                                val actionMatch = """action="([^"]+)"""".toRegex().find(html)
                                if (actionMatch != null) {
                                    var finalUrl = actionMatch.groupValues[1]
                                    if (finalUrl.startsWith("/")) {
                                        finalUrl = "https://drive.google.com" + finalUrl
                                    }
                                    
                                    val params = mutableListOf<String>()
                                    val inputRegex = """<input\s+type="hidden"\s+name="([^"]+)"\s+value="([^"]*)">""".toRegex()
                                    for (match in inputRegex.findAll(html)) {
                                        params.add(match.groupValues[1] + "=" + match.groupValues[2])
                                    }
                                    if (params.isNotEmpty()) {
                                        finalUrl += "?" + params.joinToString("&")
                                    }
                                    
                                    return@Resolver dataSpec.buildUpon().setUri(android.net.Uri.parse(finalUrl)).build()
                                }
                            }
                            connection.disconnect()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    dataSpec
                }
                
                val dataSourceFactory = androidx.media3.datasource.ResolvingDataSource.Factory(baseDataSourceFactory, resolver)
                val mediaItem = MediaItem.Builder()
                    .setUri(videoUrl)
                    .build()
                
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
                
                addListener(object : androidx.media3.common.Player.Listener {
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        playerError = error
                    }
                })
            }
    }

    BackHandler {
        onNavigateBack()
    }

    DisposableEffect(lifecycleOwner) {
        val window = activity?.window
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (exoPlayer.playWhenReady) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (window != null) {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
                androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (playerError != null) {
            androidx.compose.material3.Text(
                text = "Error: ${playerError.toString()}",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
    }
}
