package com.example.chowrate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class YelpReviews(
    val yelpReviews: List<YelpReview>,
    val total: Int,
    val possibleLanguages: List<String>
) {
    @Entity(tableName = "Review")
    data class YelpReview(
        @PrimaryKey
        val id: String,
        val url: String,
        val restaurantAlias: String,
        val text: String,
        val rating: Int,
        val timeCreated: String,
        val user: User
    ) {
        @Entity(tableName = "User")
        data class User(
            @PrimaryKey
            val id: String,
            val profileUrl: String?,
            val imageUrl: String?,
            val name: String
        )
    }
}




