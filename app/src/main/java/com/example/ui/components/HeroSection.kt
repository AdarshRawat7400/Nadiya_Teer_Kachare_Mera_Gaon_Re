package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.Book
import com.example.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness

@Composable
fun HeroSection(book: Book, themeMode: com.example.ui.theme.AppThemeMode, onToggleTheme: () -> Unit, onReadClick: () -> Unit, onListenClick: () -> Unit, onVimochanClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
    ) {
        // Dynamic Blur Background (Simulated with Gradient here for premium feel)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            com.example.ui.components.TooltipFilledIconButton(
                tooltipText = "थीम बदलें",
                onClick = onToggleTheme,
                colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                val icon = when (themeMode) {
                    com.example.ui.theme.AppThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness
                    com.example.ui.theme.AppThemeMode.LIGHT -> Icons.Filled.LightMode
                    com.example.ui.theme.AppThemeMode.DARK -> Icons.Filled.DarkMode
                    com.example.ui.theme.AppThemeMode.SEPIA -> Icons.Filled.Palette
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle Theme"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .padding(top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeroSlideshow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Increased height to utilize space
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✍️ " + book.author,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReadClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("📖 Read Book", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                OutlinedButton(
                    onClick = onListenClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("🎧 Listen", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                OutlinedButton(
                    onClick = onVimochanClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("🎬 Vimochan", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
        }
    }
}
