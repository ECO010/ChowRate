package com.example.chowrate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chowrate.data.model.YelpReviews

@Dao
interface YelpReviewDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNewReview(yelpReview : YelpReviews.YelpReview)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(restaurants: List<YelpReviews.YelpReview>)

    @Update
    suspend fun updateReview(yelpReview : YelpReviews.YelpReview)
    @Delete
    suspend fun deleteReview(yelpReview : YelpReviews.YelpReview)

    @Query("SELECT * FROM review WHERE restaurantAlias = :businessAlias")
    fun getAllReviews(businessAlias : String) : List<YelpReviews.YelpReview>

    @Query("DELETE FROM review WHERE restaurantAlias = :business_id_or_alias")
    suspend fun deleteAllReviews(business_id_or_alias: String)
}