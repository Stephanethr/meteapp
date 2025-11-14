package com.example.meteapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteapp.data.local.entities.FavoriteCityEntity
import com.example.meteapp.data.repository.Result
import com.example.meteapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: WeatherRepository) : ViewModel() {
    private val _favorites = MutableStateFlow<List<FavoriteCityEntity>>(emptyList())
    val favorites: StateFlow<List<FavoriteCityEntity>> = _favorites.asStateFlow()

    private val _searchResults = MutableStateFlow<Result.Success<Any>?>(null)

    init {
        viewModelScope.launch {
            repo.observeFavorites().collect { list ->
                _favorites.value = list
            }
        }
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            val res = repo.searchCity(query)
            // For now, we don't map; keep simple
            when (res) {
                is Result.Success -> {
                    // no-op
                }
                is Result.Error -> {
                    // no-op
                }
            }
        }
    }

    fun addFavorite(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            repo.addFavorite(FavoriteCityEntity(name = name, latitude = lat, longitude = lon))
        }
    }
}

