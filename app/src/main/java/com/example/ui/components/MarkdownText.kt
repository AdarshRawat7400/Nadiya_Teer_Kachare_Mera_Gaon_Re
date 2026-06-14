package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(line: String, defaultColor: androidx.compose.ui.graphics.Color) {
    var currentIndex = 0
    var i = 0
    while (i < line.length) {
        if (i + 1 < line.length && line[i] == '*' && line[i+1] == '*') {
            val end = line.indexOf("**", i + 2)
            if (end != -1) {
                append(line.substring(currentIndex, i))
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(line.substring(i + 2, end))
                pop()
                i = end + 2
                currentIndex = i
                continue
            }
        }
        if (i < line.length && line[i] == '*') {
            val end = line.indexOf("*", i + 1)
            if (end != -1 && end > i && line.substring(i+1, end).trim().isNotEmpty()) {
                append(line.substring(currentIndex, i))
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                append(line.substring(i + 1, end))
                pop()
                i = end + 1
                currentIndex = i
                continue
            }
        }
        if (i < line.length && line[i] == '`') {
            val end = line.indexOf("`", i + 1)
            if (end != -1 && end > i) {
                append(line.substring(currentIndex, i))
                pushStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, background = defaultColor.copy(alpha=0.1f)))
                append(line.substring(i + 1, end))
                pop()
                i = end + 1
                currentIndex = i
                continue
            }
        }
        i++
    }
    if (currentIndex < line.length) {
        append(line.substring(currentIndex))
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground
) {
    val h1Style = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    val h2Style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val h3Style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    
    val annotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            var parsedLine = line
            var currentStyle: SpanStyle? = null
            
            if (parsedLine.startsWith("# ")) {
                parsedLine = parsedLine.removePrefix("# ")
                currentStyle = h1Style
            } else if (parsedLine.startsWith("## ")) {
                parsedLine = parsedLine.removePrefix("## ")
                currentStyle = h2Style
            } else if (parsedLine.startsWith("### ")) {
                parsedLine = parsedLine.removePrefix("### ")
                currentStyle = h3Style
            } else if (parsedLine.trim() == "---") {
                parsedLine = "───────────────"
            }

            if (currentStyle != null) {
                pushStyle(currentStyle)
            }
            
            parseInlineMarkdown(parsedLine, color)
            
            if (currentStyle != null) {
                pop()
            }
            
            if (index < lines.size - 1) {
                append("\n")
            }
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
