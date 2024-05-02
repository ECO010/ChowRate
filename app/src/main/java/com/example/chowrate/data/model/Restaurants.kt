package com.example.chowrate.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

data class Restaurants(
    val restaurants: List<Restaurant>,
    val total: Int,
    val region: Region
) {
    @Entity(tableName = "Restaurant")
    data class Restaurant(
        @PrimaryKey
        val alias: String,
        val name: String,
        val image_url: String,
        val is_closed: Boolean,
        val url: String,
        val review_count: Int,
        val categories: List<Category>,
        val rating: Float,
        val coordinates: Coordinates,
        val price: String?,
        val location: Location,
        val phone: String,
        val display_phone: String,
    ) {
        @Entity(tableName = "Category")
        data class Category(
            @PrimaryKey
            val alias: String,
            val title: String
        )

        data class Coordinates(
            val latitude: Double,
            val longitude: Double
        )

        data class Location(
            val address1: String,
            val address2: String?,
            val address3: String?,
            val city: String,
            val zip_code: String,
            val country: String,
            val state: String,
            val display_address: List<String>
        )
    }

    data class Region(
        val center: Center
    ) {
        data class Center(
            val longitude: Double,
            val latitude: Double
        )
    }
}

