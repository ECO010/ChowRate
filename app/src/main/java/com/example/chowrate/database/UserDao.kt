package com.example.chowrate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chowrate.data.model.YelpReviews

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: YelpReviews.YelpReview.User)

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: String): YelpReviews.YelpReview.User?
}

