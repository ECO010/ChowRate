package com.example.chowrate.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chowrate.data.model.Restaurants
import com.example.chowrate.database.AppDatabase
import com.example.chowrate.retrofit.RetrofitInstance
import com.google.gson.JsonElement
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantViewModel(private val appDatabase: AppDatabase) : ViewModel() {
    // LiveData to hold the list of restaurants
    private var _restaurantList = MutableLiveData<List<Restaurants.Restaurant>>()
    val restaurantList: LiveData<List<Restaurants.Restaurant>>
        get() = _restaurantList

    // Custom method to handle null data fetched from the API
    fun JsonElement?.safeAsString(): String {
        return if (this != null && !isJsonNull) {
            asString
        } else {
            ""
        }
    }

    // calls the YelpFusionAPI and fetches restaurant information
    fun fetchRestaurants() {
        val limit = 30
        val apiKey = "Bearer kY0Px2Qaz6IhPHuZ6qeU7RtYPmhUXo8z-dqGcuVWwZTR7EBbi_f2EfOjWSQEgwj86CF5XFPPniA9Iz44ZDYg4_XUh0QNXTm-Ij1BI1-dlKJGEisccllEOEmhXVJwZXYx"
        val radius = 20000
        val term = "food"
        val sort_by = "best_match"
        val location = "Swansea"

        RetrofitInstance.api.getRestaurants(location, radius, term, sort_by, limit, apiKey)
            .enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        val restaurantResponse = response.body()
                        val restaurantList = parseRestaurants(restaurantResponse)
                        _restaurantList.postValue(restaurantList)

                        // Insert fetched restaurants into Room database
                        viewModelScope.launch {
                            insertRestaurants(restaurantList)
                        }

                    } else {
                        Log.d("TESTNotSuccessful", "Not successful")
                        Log.d("TESTNotSuccessful", "Response code: ${response.code()}")
                        Log.d("TESTNotSuccessful", "Error message: ${response.message()}")

                        // If there's an error body, you can also log it
                        val errorBody = response.errorBody()?.string()
                        Log.d("TESTNotSuccessful", "Error body: $errorBody")
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    TODO("Not yet implemented")
                    Log.d("Home Fragment", t.message.toString())
                }
            })
    }

    // parses info from the API call
    private fun parseRestaurants(restaurantResponse: JsonElement?): List<Restaurants.Restaurant> {
        val restaurants = mutableListOf<Restaurants.Restaurant>()
        restaurantResponse?.let { response ->
            val restaurantsAsJSONArray = response.asJsonObject.getAsJsonArray("businesses")

            // Handle parsing and processing as needed
            // Iterate through the array
            for (restaurantElement in restaurantsAsJSONArray) {
                // Initialize the JSON Object
                val restaurantObject = restaurantElement.asJsonObject

                // Get useful info that isn't nested
                val name = restaurantObject.get("name")?.asString ?: ""
                val alias = restaurantObject.get("alias")?.asString ?: ""
                val image_url = restaurantObject.get("image_url")?.asString ?: ""
                val price = restaurantObject.get("price")?.asString ?: ""
                val rating = restaurantObject.get("rating")?.asFloat ?: 0.0f
                val is_closed = restaurantObject.get("is_closed").asBoolean
                val url = restaurantObject.get("url")?.asString ?: ""
                val review_count = restaurantObject.get("review_count")?.asInt ?: 0
                val phone = restaurantObject.get("phone")?.asString ?: ""
                val display_phone = restaurantObject.get("display_phone")?.asString ?: ""

                // Extract nested coordinates
                val coordinatesObject = restaurantObject.getAsJsonObject("coordinates")
                val latitude = coordinatesObject.get("latitude")?.asDouble ?: 0.0
                val longitude = coordinatesObject.get("longitude")?.asDouble ?: 0.0
                val coordinates = Restaurants.Restaurant.Coordinates(latitude, longitude)

                // Extract nested categories
                val categoriesArray = restaurantObject.getAsJsonArray("categories")
                val categoriesList = mutableListOf<Restaurants.Restaurant.Category>()
                for (categoryElement in categoriesArray) {
                    val categoryObject = categoryElement.asJsonObject
                    val alias = categoryObject.get("alias")?.asString ?: ""
                    val title = categoryObject.get("title")?.asString ?: ""
                    val category = Restaurants.Restaurant.Category(alias, title)
                    categoriesList.add(category)
                }

                // Extract nested location
                val locationObject = restaurantObject.getAsJsonObject("location")
                val address1 = locationObject.get("address1").safeAsString()
                val address2 = locationObject.get("address2").safeAsString()
                val address3 = locationObject.get("address3").safeAsString()
                val city = locationObject.get("city")?.asString ?: ""
                val zipCode = locationObject.get("zip_code")?.asString ?: ""
                val country = locationObject.get("country")?.asString ?: ""
                val state = locationObject.get("state")?.asString ?: ""

                // Create List of address lines
                val displayAddressArray = locationObject.getAsJsonArray("display_address")
                val displayAddressList = mutableListOf<String>()
                displayAddressArray?.forEach { addressElement ->
                    addressElement.asString?.let { displayAddressList.add(it) }
                }

                // Create Location object using parsed data
                val location = Restaurants.Restaurant.Location(
                    address1, address2, address3, city, zipCode, country, state, displayAddressList
                )

                // Create Restaurant object using parsed data and add restaurants to the list
                val restaurant = Restaurants.Restaurant(
                    alias, name, image_url, is_closed, url, review_count, categoriesList, rating, coordinates,
                    price, location, phone, display_phone
                )
                restaurants.add(restaurant)
            }
        }
        return restaurants
    }

    // observes the restaurant list live data
    fun observeRestaurantLiveData():LiveData<List<Restaurants.Restaurant>> {
        return restaurantList
    }

    suspend fun insertRestaurants(restaurants: List<Restaurants.Restaurant>) {
        appDatabase.restaurantDao().insertRestaurants(restaurants)
    }
}

