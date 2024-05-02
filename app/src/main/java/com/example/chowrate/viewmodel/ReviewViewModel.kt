package com.example.chowrate.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chowrate.data.model.YelpReviews
import com.example.chowrate.database.AppDatabase
import com.example.chowrate.retrofit.RetrofitInstance
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewViewModel(private val appDatabase: AppDatabase) : ViewModel() {

    // LiveData to hold the up to date reviews
    private var _Yelp_reviewsLiveData = MutableLiveData<List<YelpReviews.YelpReview>>()
    val yelpReviewList: LiveData<List<YelpReviews.YelpReview>>
        get() = _Yelp_reviewsLiveData


    // Custom method to handle null data fetched from the API e.g user imageUrl
    fun JsonElement?.safeAsString(): String {
        return if (this != null && !isJsonNull) {
            asString
        } else {
            ""
        }
    }

    // calls the YelpFusionAPI to fetch reviews for a particular restaurant id/alias
    // adds it to the DB
    fun fetchReviews(business_id_or_alias: String): List<YelpReviews.YelpReview> {
        val apiKey = "Bearer kY0Px2Qaz6IhPHuZ6qeU7RtYPmhUXo8z-dqGcuVWwZTR7EBbi_f2EfOjWSQEgwj86CF5XFPPniA9Iz44ZDYg4_XUh0QNXTm-Ij1BI1-dlKJGEisccllEOEmhXVJwZXYx"
        val limit = 20
        val sort_by = "yelp_sort"

        val yelpReviews = mutableListOf<YelpReviews.YelpReview>()
        RetrofitInstance.api.getReviews(business_id_or_alias,sort_by,limit, apiKey)
            .enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        val reviewResponse = response.body()
                        if (reviewResponse != null) {
                            val reviewsArray = reviewResponse.asJsonObject.getAsJsonArray("reviews")

                            // Iterate through the array of reviews
                            for (reviewElement in reviewsArray) {
                                // Initialize the JSON Object
                                val reviewObject = reviewElement.asJsonObject

                                // Get review details
                                val id = reviewObject.get("id")?.asString ?: ""
                                val url = reviewObject.get("url")?.asString ?: ""
                                val text = reviewObject.get("text")?.asString ?: ""
                                val rating = reviewObject.get("rating")?.asInt ?: 0
                                val timeCreated = reviewObject.get("time_created")?.asString ?: ""

                                // Get user details
                                val userObject = reviewObject.getAsJsonObject("user")
                                val userId = userObject.get("id")?.asString ?: ""
                                val profileUrl = userObject.get("profile_url")?.asString ?: ""
                                val imageUrl = userObject.get("image_url").safeAsString()
                                val userName = userObject.get("name")?.asString ?: ""

                                // Create Review object using parsed data and add reviews to the list
                                val user = YelpReviews.YelpReview.User(userId, profileUrl, imageUrl, userName)
                                val yelpReview = YelpReviews.YelpReview(id, url, business_id_or_alias, text, rating, timeCreated, user)
                                yelpReviews.add(yelpReview)

                                viewModelScope.launch(Dispatchers.IO) {
                                    insertReview(yelpReview)
                                    // Fetch reviews from DB
                                    val reviewsFromDB = fetchReviewsFromDB(business_id_or_alias)
                                    // Combine API-fetched and DB reviews
                                    val combinedReviews = combineReviewLists(yelpReviews, reviewsFromDB)
                                    // Update LiveData with combined reviews
                                    _Yelp_reviewsLiveData.postValue(combinedReviews)
                                }
                            }
                        }
                    }
                    else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("TESTNotSuccessful", "Not successful")
                        Log.d("TESTNotSuccessful", "Response code: ${response.code()}")
                        Log.d("TESTNotSuccessful", "Error message: ${response.message()}")
                        Log.d("TESTNotSuccessful", "Error body: $errorBody")
                        return
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Log.d("RestaurantOverview", t.message.toString())
                }
            })
        return yelpReviews
    }

    // observes the review list live data
    fun observeReviewLiveData() : LiveData<List<YelpReviews.YelpReview>> {
        return yelpReviewList
    }

    fun insertReview(review: YelpReviews.YelpReview) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.yelpReviewDao().addNewReview(review)
        }
    }

    suspend fun fetchReviewsFromDB(business_id_or_alias: String): List<YelpReviews.YelpReview> {
        return withContext(Dispatchers.IO) {
            appDatabase.yelpReviewDao().getAllReviews(business_id_or_alias)
        }
    }


    fun combineReviewLists(reviewsFromAPI: List<YelpReviews.YelpReview>?, reviewsFromDB: List<YelpReviews.YelpReview>?) : List<YelpReviews.YelpReview> {
        val combinedReviews = mutableListOf<YelpReviews.YelpReview>()

        // Add reviews from API
        reviewsFromAPI?.forEach { review ->
            if (!combinedReviews.any { combinedReview -> combinedReview.id == review.id }) {
                combinedReviews.add(review)
            }
        }

        // Add reviews from DB that are not in API
        if (reviewsFromDB != null) {
            reviewsFromDB.forEach { review ->
                if (!combinedReviews.any { combinedReview -> combinedReview.id == review.id }) {
                    combinedReviews.add(review)
                }
            }
        }

        return combinedReviews
    }

}