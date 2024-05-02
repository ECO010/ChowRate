package com.example.chowrate.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chowrate.R
import com.example.chowrate.data.model.FavoritesList
import com.example.chowrate.data.model.RestaurantAdapter
import com.example.chowrate.data.model.Restaurants
import com.example.chowrate.database.AppDatabase
import com.example.chowrate.retrofit.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SavedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedFragment : Fragment() {
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser
    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private var restaurantList: List<Restaurants.Restaurant> = mutableListOf()
    private lateinit var loginToAccessTextView: TextView
    private lateinit var createAccountToAccessTextView: TextView
    private lateinit var orTextView: TextView
    private lateinit var addToThisListBtn: Button
    private lateinit var appDatabase : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //callAPI()
        //showSavedRestaurants()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_saved, container, false)

        //Initialize layout components
        recyclerView = view.findViewById(R.id.recyclerSavedRestaurants)
        loginToAccessTextView = view.findViewById(R.id.loginToAccessTextView)
        createAccountToAccessTextView = view.findViewById(R.id.createAccountToAccessTextView)
        orTextView = view.findViewById(R.id.OrTextView2)
        addToThisListBtn = view.findViewById(R.id.AddToThisListBtn)

        changeLayoutBasedOnUser()

        // TODO: Set OnClick Listeners for the add to list button to make it do something
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sort restaurantList by ratings in descending order
        (restaurantList as MutableList<Restaurants.Restaurant>).sortByDescending { it.rating }

        //recyclerView = view.findViewById(R.id.recyclerSavedRestaurants)
        restaurantAdapter = RestaurantAdapter(restaurantList)
        recyclerView.adapter = restaurantAdapter
        val horizontalLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = horizontalLayoutManager
    }

    // Custom method to handle null data fetched from the API
    fun JsonElement?.safeAsString(): String {
        return if (this != null && !isJsonNull) {
            asString
        } else {
            ""
        }
    }

    private fun showSavedRestaurants() {
        // Get saved restaurants for the current user
        val savedRestaurants = appDatabase.restaurantDao().getAllRestaurants()
    }

    //Method calls
   /* private fun callAPI() {
        val limit = 7
        val apiKey = "Bearer kY0Px2Qaz6IhPHuZ6qeU7RtYPmhUXo8z-dqGcuVWwZTR7EBbi_f2EfOjWSQEgwj86CF5XFPPniA9Iz44ZDYg4_XUh0QNXTm-Ij1BI1-dlKJGEisccllEOEmhXVJwZXYx"
        val radius = 20000
        val term = "food"
        val sort_by = "best_match"
        val location = "Swansea"
        RetrofitInstance.api.getRestaurants(location, radius, term, sort_by, limit, apiKey).enqueue(object:
            Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    Log.d("TESTSuccessful", "successful")
                    // Handle successful response
                    val restaurantResponse = response.body()
                    Log.d("TESTResponse", restaurantResponse.toString())
                    if (restaurantResponse != null) {
                        val restaurantsAsJSONArray = restaurantResponse.asJsonObject.getAsJsonArray("businesses")

                        // Handle parsing and processing as needed
                        // Iterate through the array
                        Log.d("TESTJSONArray", restaurantsAsJSONArray.toString())

                        for (restaurantElement in restaurantsAsJSONArray) {
                            // Initialize the JSON Object
                            val restaurantObject = restaurantElement.asJsonObject

                            // Get useful info that isn't nested
                            Log.d("TESTRestaurantObject", restaurantObject.toString())
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
                            Log.d("TESTCoordinates", coordinatesObject.toString())

                            // Extract nested categories
                            val categoriesArray = restaurantObject.getAsJsonArray("categories")
                            Log.d("TESTCategoriesArray", categoriesArray.toString())
                            val categoriesList = mutableListOf<Restaurants.Restaurant.Category>()
                            for (categoryElement in categoriesArray) {
                                val categoryObject = categoryElement.asJsonObject
                                val alias = categoryObject.get("alias")?.asString ?: ""
                                val title = categoryObject.get("title")?.asString ?: ""
                                val category = Restaurants.Restaurant.Category(alias, title)
                                categoriesList.add(category)
                                Log.d("TESTCategoriesList", categoriesList.toString())
                            }

                            // Extract nested location
                            val locationObject = restaurantObject.getAsJsonObject("location")
                            Log.d("TESTLocation", locationObject.toString())
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
                            val restaurant = Restaurants.Restaurant(alias, name,image_url,is_closed,url,review_count,categoriesList,rating,coordinates,price,location,phone,display_phone)
                            (restaurantList as MutableList<Restaurants.Restaurant>).add(restaurant)

                            // Sort restaurantList by descending order of ratings
                            (restaurantList as MutableList<Restaurants.Restaurant>).sortByDescending { it.rating }
                            Log.d("TESTRestaurantList", restaurantList.toString())
                        }
                    }

                    // Pass the restaurant list to the RecyclerView adapter and display in the UI
                    // Inside your Home Fragment where you fetched the restaurant list
                    val recyclerView: RecyclerView =
                        view?.findViewById(R.id.recyclerViewRestaurants) ?: recyclerView
                    val horizontalLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.layoutManager = horizontalLayoutManager

                    val restaurantAdapter = RestaurantAdapter(restaurantList)
                    recyclerView.adapter = restaurantAdapter
                    restaurantAdapter.updateRestaurants(restaurantList)
                }
                else {
                    Log.d("TESTNotSuccessful", "Not successful")
                    Log.d("TESTNotSuccessful", "Response code: ${response.code()}")
                    Log.d("TESTNotSuccessful", "Error message: ${response.message()}")
                    return
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                TODO("Not yet implemented")
                Log.d("Home Fragment", t.message.toString())
            }
        })
    }*/

    private fun changeLayoutBasedOnUser () {
        if (currentUser != null) {
            orTextView.visibility = View.GONE
            loginToAccessTextView.visibility = View.GONE
            createAccountToAccessTextView.visibility = View.GONE
            addToThisListBtn.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
        }
        else {
            orTextView.visibility = View.VISIBLE
            loginToAccessTextView.visibility = View.VISIBLE
            createAccountToAccessTextView.visibility = View.VISIBLE
            addToThisListBtn.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

}