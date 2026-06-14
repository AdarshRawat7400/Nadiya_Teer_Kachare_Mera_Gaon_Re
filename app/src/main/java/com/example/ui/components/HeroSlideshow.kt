package com.example.ui.components

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun HeroSlideshow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var images by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val assets = context.assets.list("slideshows")
            if (!assets.isNullOrEmpty()) {
                // Prepend the required path prefix for Coil
                images = assets.map { "file:///android_asset/slideshows/$it" }
            } else {
                images = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            images = emptyList()
        }
    }

    if (images.isEmpty()) {
        // Render empty placeholder taking up the same space to avoid jumping layout
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
        }
    }

    if (images.isNotEmpty()) {
        val actualPageCount = images.size
        // Use a safe multiplier so it doesn't break pager internals
        val virtualCount = actualPageCount * 400
        val initialPage = actualPageCount * 200
        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { virtualCount }
        )
        val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

        // Auto-scroll logic
        LaunchedEffect(isDragged) {
            if (!isDragged) {
                while (true) {
                    delay(4000)
                    // Use settledPage to ensure we move to the next full page
                    val target = if (pagerState.currentPage == pagerState.settledPage) {
                        pagerState.currentPage + 1
                    } else {
                        pagerState.settledPage + 1
                    }
                    pagerState.animateScrollToPage(
                        page = target,
                        animationSpec = tween(durationMillis = 800)
                    )
                }
            }
        }

        Box(modifier = modifier) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 2 // Preload neighboring images
            ) { page ->
                val virtualIndex = page % actualPageCount
                val imageUrl = images[virtualIndex]

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Calculate the absolute offset for the current page from the
                            // scroll position. We use the fractional part of the
                            // currentPageOffsetFraction.
                            val pageOffset = (
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).absoluteValue

                            // Alpha effect for fade transition
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background (provides context colors, replacing blur which crashed the emulator)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Dark tint to ensure the main image stands out
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f))
                        )

                        // Main focused image
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Slideshow image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Indicator Dots
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentActualPage = pagerState.currentPage % actualPageCount
                repeat(actualPageCount) { iteration ->
                    val color = if (currentActualPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.LightGray.copy(alpha = 0.8f)
                    }
                    val width = if (currentActualPage == iteration) 16.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .height(8.dp)
                            .width(width)
                    )
                }
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}
