package com.example.chowrate.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.chowrate.data.model.Restaurants
import com.example.chowrate.data.model.YelpReviews
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@TypeConverters
class TypeConverter {

    @TypeConverter
    fun fromUserToString(user: YelpReviews.YelpReview.User) : String {
        val gson = Gson()
        return gson.toJson(user)
    }

    @TypeConverter
    fun fromStringToUser(userString: String): YelpReviews.YelpReview.User {
        val gson = Gson()
        return gson.fromJson(userString, YelpReviews.YelpReview.User::class.java)
    }

    @TypeConverter
    fun fromCategoriesToString(categories: List<Restaurants.Restaurant.Category>): String {
        val gson = Gson()
        return gson.toJson(categories)
    }

    @TypeConverter
    fun fromStringToCategories(categoriesString: String): List<Restaurants.Restaurant.Category> {
        val gson = Gson()
        val itemType = object : TypeToken<List<Restaurants.Restaurant.Category>>() {}.type
        return gson.fromJson(categoriesString, itemType)
    }

    @TypeConverter
    fun fromLocationToString(location: Restaurants.Restaurant.Location): String {
        val gson = Gson()
        return gson.toJson(location)
    }

    @TypeConverter
    fun fromStringToLocation(locationString: String): Restaurants.Restaurant.Location {
        val gson = Gson()
        return gson.fromJson(locationString, Restaurants.Restaurant.Location::class.java)
    }

    @TypeConverter
    fun fromCoordinatesToString(coordinates: Restaurants.Restaurant.Coordinates): String {
        val gson = Gson()
        return gson.toJson(coordinates)
    }

    @TypeConverter
    fun fromStringToCoordinates(coordinatesString: String): Restaurants.Restaurant.Coordinates {
        val gson = Gson()
        return gson.fromJson(coordinatesString, Restaurants.Restaurant.Coordinates::class.java)
    }

}



