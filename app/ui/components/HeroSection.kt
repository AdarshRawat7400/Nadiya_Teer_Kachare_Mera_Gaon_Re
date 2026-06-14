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

@Composable
fun HeroSection(book: Book, isDarkMode: Boolean, onToggleTheme: () -> Unit, onReadClick: () -> Unit, onListenClick: () -> Unit) {
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

        androidx.compose.material3.FilledIconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book Cover with elegant shadow
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.book_cover),
                contentDescription = "Book Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(260.dp)
                    .width(180.dp)
                    .shadow(24.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("📖 Read Book", style = MaterialTheme.typography.titleMedium)
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
                    )
                ) {
                    Text("🎧 Listen", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
