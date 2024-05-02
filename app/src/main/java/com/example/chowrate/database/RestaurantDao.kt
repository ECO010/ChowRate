package com.example.chowrate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chowrate.data.model.Restaurants

@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addRestaurant(restaurant : Restaurants.Restaurant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<Restaurants.Restaurant>)

    @Delete
    suspend fun removeRestaurant(restaurant: Restaurants.Restaurant)

    @Query("SELECT * FROM Restaurant")
    fun getAllRestaurants() : List<Restaurants.Restaurant>

    @Query("SELECT name FROM Restaurant")
    fun getAllRestaurantNames() : List<String>

    @Query("SELECT * FROM Category")
    fun getAllCategories() : List<Restaurants.Restaurant.Category>

    @Query("SELECT * FROM restaurant WHERE alias = :business_id_alias")
    fun getRestaurantByIdAlias(business_id_alias: String): Restaurants.Restaurant?

    @Query("SELECT alias FROM restaurant WHERE name = :name")
    fun getRestaurantAliasFromName(name: String): String
}