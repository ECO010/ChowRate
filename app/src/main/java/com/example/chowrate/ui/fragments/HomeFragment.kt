package com.example.chowrate.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chowrate.R
import com.example.chowrate.data.model.RestaurantAdapter
import com.example.chowrate.data.model.Restaurants
import com.example.chowrate.database.AppDatabase
import com.example.chowrate.databinding.FragmentHomeBinding
import com.example.chowrate.ui.activities.RestaurantOverviewActivity
import com.example.chowrate.viewmodel.RestaurantViewModel
import com.example.chowrate.viewmodel.RestaurantViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    // declaring fragment components
    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var restaurantViewModel: RestaurantViewModel
    private var restaurantList: List<Restaurants.Restaurant> = mutableListOf()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: FragmentHomeBinding

    // declare and initialize companion objects that will be used in the restaurant overview page
    companion object {
      const val RESTAURANT_ID = "com.example.chowrate.ui.fragments.RestaurantId"
      const val RESTAURANT_NAME = "com.example.chowrate.ui.fragments.RestaurantName"
      const val RESTAURANT_IMG = "com.example.chowrate.ui.fragments.RestaurantPic"
      const val RESTAURANT_ADDRESS = "com.example.chowrate.ui.fragments.RestaurantAddress"
      const val RESTAURANT_PRICE = "com.example.chowrate.ui.fragments.RestaurantPrice"
      const val RESTAURANT_RATING = "com.example.chowrate.ui.fragments.RestaurantRating"
        const val RESTAURANT_LATITUDE = "com.example.chowrate.ui.fragments.RestaurantLatitude"
        const val RESTAURANT_LONGITUDE = "com.example.chowrate.ui.fragments.RestaurantLongitude"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding = FragmentHomeBinding.inflate(layoutInflater)

        // Initialize an instance of the app DB and the restaurant view model factory
        val appDatabase = AppDatabase.getInstance(requireActivity())
        val viewModelFactory = RestaurantViewModelFactory(appDatabase)
        restaurantViewModel = ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel::class.java)

        // use the restaurant view model to fetch restaurants to be displayed
        restaurantViewModel.fetchRestaurants()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize recycler view
        recyclerView = view.findViewById(R.id.recyclerViewRestaurants)

        // Observe LiveData from ViewModel
        observeRestaurantData()

        // Initialize adapter with restaurant list
        restaurantAdapter = RestaurantAdapter(restaurantList)

        // Set item click listener
        restaurantAdapter.setOnItemClickListener { position ->
            val intent = Intent(requireContext(), RestaurantOverviewActivity::class.java)
            val appDatabase = AppDatabase.getInstance(requireActivity())
            val viewModelFactory = RestaurantViewModelFactory(appDatabase)
            val restaurantViewModel = ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel::class.java)

            // Observe the LiveData directly from the ViewModel
            restaurantViewModel.restaurantList.observe(viewLifecycleOwner) { restaurantList ->
                // Get the specific restaurant based on the position
                val specificRestaurant = restaurantList[position]

                // Get the details needed from the specific restaurant and pass them to the intent (Restaurant Overview)
                val restaurantName = specificRestaurant.name
                val restaurantImg = specificRestaurant.image_url
                val restaurantIdAlias = specificRestaurant.alias
                val restaurantRating = specificRestaurant.rating
                val restaurantPrice = specificRestaurant.price
                val longitude = specificRestaurant.coordinates.longitude
                val latitude = specificRestaurant.coordinates.latitude
                val restaurantAddress = specificRestaurant.location.display_address

                intent.putExtra("restaurantPosition", position)
                intent.putExtra(RESTAURANT_ID, restaurantIdAlias)
                intent.putExtra(RESTAURANT_NAME, restaurantName)
                intent.putExtra(RESTAURANT_IMG, restaurantImg)
                intent.putExtra(RESTAURANT_ADDRESS, restaurantAddress.toTypedArray())
                intent.putExtra(RESTAURANT_RATING,restaurantRating.toString())
                intent.putExtra(RESTAURANT_PRICE, restaurantPrice)
                intent.putExtra(RESTAURANT_LATITUDE, latitude)
                intent.putExtra(RESTAURANT_LONGITUDE, longitude)
                startActivity(intent)

                // Testing in Logcat to make sure the right data was being retrieved
                Log.d("TESTName", restaurantName)
                Log.d("TESTId", restaurantIdAlias)
                Log.d("TESTPic", restaurantImg)
                Log.d("TESTLong", longitude.toString())
                Log.d("TESTLat", latitude.toString())
                Log.d("TESTRating", restaurantRating.toString())
                Log.d("TESTAddress", restaurantAddress.toString())
                Log.d("TESTRestaurantPosition", position.toString())
            }
        }
        // Set the adapter to the recycler view
        recyclerView.adapter = restaurantAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // The updated list of restaurants is handled here
    // Update the adapter with the new list of restaurants
    private fun observeRestaurantData() {
        restaurantViewModel.observeRestaurantLiveData().observe(viewLifecycleOwner, object : Observer<List<Restaurants.Restaurant>> {
            override fun onChanged(value: List<Restaurants.Restaurant>) {
                value?.let { restaurants ->
                    restaurantAdapter.updateRestaurants(restaurants)
                }
            }
        })
    }
}