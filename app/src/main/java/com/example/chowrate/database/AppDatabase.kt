package com.example.chowrate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chowrate.data.model.Restaurants
import com.example.chowrate.data.model.YelpReviews

@Database(entities = [YelpReviews.YelpReview::class, Restaurants.Restaurant::class, Restaurants.Restaurant.Category::class, YelpReviews.YelpReview.User::class],version = 2)
@TypeConverters(TypeConverter::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun yelpReviewDao(): YelpReviewDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun userDao(): UserDao

    companion object {
        // Create an instance of the Room database
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app-database"
            ).fallbackToDestructiveMigration().build()
        }
    }
}