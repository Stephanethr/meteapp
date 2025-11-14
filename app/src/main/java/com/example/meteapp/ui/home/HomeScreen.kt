package com.example.meteapp.ui.home

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import com.example.meteapp.data.local.entities.FavoriteCityEntity
import com.example.meteapp.data.remote.GeocodingResult
import com.example.meteapp.data.repository.Result
import com.example.meteapp.data.repository.WeatherRepository
import com.example.meteapp.location.LocationHelper
import com.example.meteapp.ui.components.WeatherCard
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.rememberUpdatedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenDetail: (Double, Double, String) -> Unit = { _, _, _ -> }) {
    val context = LocalContext.current
    val activity = context as? Activity
    val repo = remember { WeatherRepository(context) }
    val locationHelper = remember { LocationHelper(context) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // permission UI state
    var showRationale by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // observe favorites
    val favoritesState = repo.observeFavorites().collectAsState(initial = emptyList())

    // map favoriteId -> summary string (temp or error)
    val favSummaries = remember { mutableStateMapOf<Long, String>() }

    // refresh summaries when favorites change
    LaunchedEffect(favoritesState.value) {
        favSummaries.clear()
        favoritesState.value.forEach { fav ->
            // launch a coroutine per favorite to fetch weather (repository caches responses)
            launch {
                try {
                    val res = repo.getWeather(fav.latitude, fav.longitude)
                    when (res) {
                        is Result.Success -> {
                            val temp = res.value.current_weather?.temperature
                            val summary = if (temp != null) "${temp} °C" else "—"
                            favSummaries[fav.id] = summary
                        }
                        is Result.Error -> favSummaries[fav.id] = "Err"
                    }
                } catch (_: Exception) {
                    favSummaries[fav.id] = "Err"
                }
            }
        }
    }

    // permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // permission granted -> fetch location
            scope.launch {
                val loc = locationHelper.getLastLocation()
                if (loc != null) {
                    repo.addFavorite(FavoriteCityEntity(name = "Ma position", latitude = loc.latitude, longitude = loc.longitude))
                } else {
                    locationError = "Impossible d'obtenir la position"
                }
            }
        } else {
            // denied -> check if permanently denied
            val shouldShow = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION) } ?: false
            if (!shouldShow) {
                // user denied permanently
                showPermissionDeniedDialog = true
            } else {
                // user denied temporarily
                locationError = "Permission non accordée"
            }
        }
    }

    fun requestLocationPermission() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            scope.launch {
                val loc = locationHelper.getLastLocation()
                if (loc != null) {
                    repo.addFavorite(FavoriteCityEntity(name = "Ma position", latitude = loc.latitude, longitude = loc.longitude))
                } else {
                    locationError = "Impossible d'obtenir la position"
                }
            }
        } else {
            val showRationaleNow = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION) } ?: false
            if (showRationaleNow) {
                showRationale = true
            } else {
                // launch system permission request
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌦️ Météo", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Search section
            Box(modifier = Modifier.background(Color.White).padding(12.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Rechercher") },
                    label = { Text("Rechercher une ville...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Actions row
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            if (query.isNotBlank()) {
                                searchError = null
                                val res = repo.searchCity(query)
                                when (res) {
                                    is Result.Success -> searchResults = res.value.results ?: emptyList()
                                    is Result.Error -> searchError = res.message
                                }
                            }
                        }
                    } ) {
                    Text("Rechercher")
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(onClick = { requestLocationPermission() }) {
                    Text("Ajouter ma position")
                }
            }

            if (locationError != null) {
                Text(text = "Erreur localisation: $locationError", modifier = Modifier.padding(8.dp))
            }

            // Search results
            if (searchError != null) {
                Text(text = "Erreur recherche: $searchError", modifier = Modifier.padding(8.dp))
            }
            // search results area - fixed fraction height to avoid Modifier.weight issues
            LazyColumn(modifier = Modifier.fillMaxHeight(0.3f)) {
                items(searchResults) { item ->
                    WeatherCard(
                        title = item.name,
                        subtitle = item.country ?: "",
                        iconCode = 0,
                        temp = null,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        scope.launch {
                            repo.addFavorite(FavoriteCityEntity(name = item.name, latitude = item.latitude, longitude = item.longitude, country = item.country))
                            searchResults = emptyList()
                            query = ""
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 8.dp))

            // Favorites list
            Text(text = "Favoris:", modifier = Modifier.padding(8.dp))
            // favorites list area — occupe l'espace restant jusqu'en bas
            var showDeleteDialog by remember { mutableStateOf<FavoriteCityEntity?>(null) }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(favoritesState.value) { fav: FavoriteCityEntity ->
                    WeatherCard(
                        title = fav.name,
                        subtitle = fav.country ?: "Favori",
                        temp = favSummaries[fav.id] ?: "—",
                        iconCode = 0,
                        modifier = Modifier
                            .padding(8.dp)
                            .pointerInput(fav.id) {
                                detectTapGestures(
                                    onLongPress = {
                                        showDeleteDialog = fav
                                    },
                                    onTap = {
                                        onOpenDetail(fav.latitude, fav.longitude, fav.name)
                                    }
                                )
                            }
                    )
                }
            }

            if (showDeleteDialog != null) {
                val toDelete = showDeleteDialog!!
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Supprimer le favori") },
                    text = { Text("Supprimer ${toDelete.name} des favoris ?") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch { repo.removeFavorite(toDelete.id) }
                            showDeleteDialog = null
                        }) { Text("Supprimer") }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Annuler") } }
                )
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) { Text("Demander") }
            },
            dismissButton = { TextButton(onClick = { showRationale = false }) { Text("Annuler") } },
            title = { Text("Permission localisation") },
            text = { Text("L'application a besoin de la permission de localisation pour ajouter votre position aux favoris.") }
        )
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    // open app settings
                    try {
                        val pkg = activity?.packageName ?: context.packageName
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", pkg, null)
                        }
                        activity?.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        // ignore
                    }
                }) { Text("Ouvrir les paramètres") }
            },
            dismissButton = { TextButton(onClick = { showPermissionDeniedDialog = false }) { Text("Annuler") } },
            title = { Text("Permission refusée") },
            text = { Text("La permission de localisation a été refusée définitivement. Ouvrez les paramètres pour l'activer.") }
        )
    }
}
