package com.example.chowrate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chowrate.database.AppDatabase

class RestaurantViewModelFactory (private val appDatabase: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return return RestaurantViewModel(appDatabase) as T
    }
}