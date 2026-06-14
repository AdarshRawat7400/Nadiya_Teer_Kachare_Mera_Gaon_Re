package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        // Match **bold** or *italic*
        val mdRegex = Regex("(\\*\\*(.*?)\\*\\*)|(\\*(.*?)\\*)")
        
        val matches = mdRegex.findAll(text)
        
        for (match in matches) {
            // Append text before the match
            if (currentIndex < match.range.first) {
                append(text.substring(currentIndex, match.range.first))
            }
            
            if (match.groupValues[1].isNotEmpty()) {
                // **bold**
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(match.groupValues[2])
                pop()
            } else if (match.groupValues[3].isNotEmpty()) {
                // *italic*
                pushStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                append(match.groupValues[4])
                pop()
            }
            
            currentIndex = match.range.last + 1
        }
        
        // Append remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        textAlign = textAlign,
        style = style,
        color = color
    )
}
