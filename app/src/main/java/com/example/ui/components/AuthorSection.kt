package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.data.models.Book

@Composable
fun AuthorSection(book: Book) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val imageModifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.author_pic),
                    contentDescription = "Author Photo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = imageModifier
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "About the Author",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "जन्म: 10 अगस्त 1934, छतरपुर।\nयोग्यता: एम.ए. (हिन्दी, इतिहास, अंग्रेजी), एम.एड.।\nशिक्षा विभाग में आपने प्राचार्य, बी.ई.ओ. एवं जिला परियोजना अधिकारी (रा.गा.शि. मिशन) सहित कई महत्वपूर्ण पदों पर कार्य किया है।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
