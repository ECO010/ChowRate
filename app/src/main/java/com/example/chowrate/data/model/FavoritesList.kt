package com.example.chowrate.data.model
// TO-DO: create Dao implement user favorites
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "FavoriteRestaurants")
data class FavoritesList (
    val restaurantDetails: Restaurants.Restaurant,
    @PrimaryKey
    val userId: String
)