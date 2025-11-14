package com.example.meteapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteapp.data.repository.Result
import com.example.meteapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val repo: WeatherRepository) : ViewModel() {
    private val _state = MutableStateFlow<Result<Any>?>(null)
    val state: StateFlow<Result<Any>?> = _state.asStateFlow()

    fun load(lat: Double, lon: Double) {
        viewModelScope.launch {
            val res = repo.getWeather(lat, lon)
            _state.value = when (res) {
                is Result.Success -> Result.Success(res.value)
                is Result.Error -> Result.Error(res.message)
            }
        }
    }
}

