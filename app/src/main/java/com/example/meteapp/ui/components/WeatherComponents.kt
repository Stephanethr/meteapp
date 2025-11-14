package com.example.meteapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherIcon(code: Int, modifier: Modifier = Modifier, size: Int = 28) {
    val symbol = when (code) {
        0 -> "☀️"
        in 1..3 -> "🌤️"
        in 45..48 -> "🌫️"
        in 51..67, in 80..82 -> "🌧️"
        in 95..99 -> "⛈️"
        else -> "❓"
    }
    Text(text = symbol, fontSize = size.sp, modifier = modifier)
}

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    temp: String? = null,
    iconCode: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    val gradientColor = when (iconCode) {
        0 -> listOf(Color(0xFF2196F3), Color(0xFF64B5F6))  // Soleil - bleu
        in 1..3 -> listOf(Color(0xFFBDBDBD), Color(0xFFF5F5F5))  // Nuages - gris
        in 45..48 -> listOf(Color(0xFF78909C), Color(0xFFB0BEC5))  // Brouillard - bleu-gris
        in 51..67, in 80..82 -> listOf(Color(0xFF1565C0), Color(0xFF42A5F5))  // Pluie - bleu foncé
        in 95..99 -> listOf(Color(0xFF37474F), Color(0xFF455A64))  // Orage - gris foncé
        else -> listOf(Color(0xFFF5F5F5), Color(0xFFFFFFFF))
    }

    Card(
        modifier = cardModifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = gradientColor))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    if (iconCode != null) {
                        WeatherIcon(code = iconCode, size = 36)
                    }
                    if (temp != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = temp,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "⚠️ $message",
            color = Color(0xFFC62828),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
