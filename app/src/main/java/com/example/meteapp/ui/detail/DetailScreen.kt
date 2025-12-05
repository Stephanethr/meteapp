package com.example.meteapp.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteapp.data.remote.ForecastResponse
import com.example.meteapp.data.repository.Result
import com.example.meteapp.data.repository.WeatherRepository
import com.example.meteapp.ui.components.WeatherCard
import com.example.meteapp.ui.components.LoadingIndicator
import com.example.meteapp.ui.components.ErrorMessage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(latitude: Double, longitude: Double, cityName: String, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val repo = remember { WeatherRepository(context) }

    val weatherResult = produceState<Result<ForecastResponse>?>(initialValue = null, latitude, longitude) {
        value = repo.getWeather(latitude, longitude)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                title = { Text(cityName, style = MaterialTheme.typography.headlineMedium) },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
        ) {
            when (val res = weatherResult.value) {
                null -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                is Result.Error -> ErrorMessage(message = res.message, modifier = Modifier.padding(16.dp))
                is Result.Success -> {
                    val data = res.value
                    val current = data.current_weather
                    val daily = data.daily
                    val tempText = current?.temperature?.let { "${it.toInt()} °C" } ?: "—"
                    val min = daily?.temperature_2m_min?.firstOrNull()
                    val max = daily?.temperature_2m_max?.firstOrNull()
                    val minMaxText = "Min: ${min?.toInt() ?: "—"}° • Max: ${max?.toInt() ?: "—"}°"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Main weather card — now shows city name
                        WeatherCard(
                            title = cityName,
                            subtitle = minMaxText,
                            temp = tempText,
                            iconCode = current?.weathercode ?: 0,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Details grid
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Coordinates
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍 Localisation", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    Text("${latitude.format(2)}, ${longitude.format(2)}", color = Color.Gray)
                                }

                                // Wind speed
                                if (current != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💨 Vitesse du vent", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                        Text("${current.windspeed.toInt()} km/h", color = Color.Gray)
                                    }
                                }

                                // Weather description
                                if (current != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🌈 Condition", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                        Text(mapWeatherCode(current.weathercode), color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun mapWeatherCode(code: Int): String {
    return when (code) {
        0 -> "Ensoleillé ☀️"
        in 1..3 -> "Partiellement nuageux 🌤️"
        in 45..48 -> "Brouillard 🌫️"
        in 51..67, in 80..82 -> "Pluvieux 🌧️"
        in 95..99 -> "Orage ⛈️"
        else -> "Inconnu ❓"
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
