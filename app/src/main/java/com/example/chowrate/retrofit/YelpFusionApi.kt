package com.example.chowrate.retrofit

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface YelpFusionApi {
    @GET("businesses/search")
    fun getRestaurants(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("term") term: String,
        @Query("sort_by") sortBy: String,
        @Query("limit") limit: Int,
        @Header("Authorization") apiKey: String
    ):Call<JsonElement>

    @GET("businesses/{business_id_or_alias}/reviews")
    fun getReviews(
        @Path("business_id_or_alias") business_id_or_alias: String,
        @Query("sort_by") sortBy: String,
        @Query("limit") limit: Int,
        @Header("Authorization") apiKey: String
    ): Call<JsonElement>

}