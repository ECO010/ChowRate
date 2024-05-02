package com.example.chowrate.ui.activities

import ReviewAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chowrate.R
import com.example.chowrate.data.model.YelpReviews
import com.example.chowrate.database.AppDatabase
import com.example.chowrate.ui.fragments.HomeFragment
import com.example.chowrate.viewmodel.ReviewViewModel
import com.example.chowrate.viewmodel.ReviewViewModelFactory
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RestaurantOverviewActivity : AppCompatActivity() {

    private lateinit var collapsingToolbarTitle: CollapsingToolbarLayout
    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var textViewRating: TextView
    private lateinit var textViewPrice: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var imageViewImage: ImageView
    private lateinit var restaurantId: String
    private lateinit var getDirectionsButton: Button
    private lateinit var restaurantName: String
    private lateinit var restaurantRating: String
    private lateinit var restaurantImg: String
    private lateinit var restaurantPrice: String
    private lateinit var recyclerViewReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private var yelpReviewList: List<YelpReviews.YelpReview> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_overview)

        //Initialize the clickable components
        getDirectionsButton = findViewById(R.id.getDirectionsButton)

        // Call method to get restaurant info (business_id_alias is needed for reviews)
        getRestaurantInfoFromIntent()

        // Initializing a HomeViewModel to fetch and observe reviews of the restaurant id
        val appDatabase = AppDatabase.getInstance(this)
        val viewModelFactory = ReviewViewModelFactory(appDatabase)
        reviewViewModel = ViewModelProvider(this, viewModelFactory).get(ReviewViewModel::class.java)

        // Fetch reviews of the specific restaurant from the API
        GlobalScope.launch(Dispatchers.IO) {
            Log.d("TESTRestaurantIdBeforeFetch", restaurantId)
            reviewViewModel.fetchReviews(restaurantId)
        }

        // Initialize RecyclerView and the adapter for reviews
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews)

        // Observe Review Data
        observeReviews()

        reviewAdapter = ReviewAdapter(yelpReviewList,this)
        recyclerViewReviews.adapter = reviewAdapter
        recyclerViewReviews.layoutManager = LinearLayoutManager(this)

        // Get Long and Let for Get directions feature
        val restaurantLat = intent.getDoubleExtra(HomeFragment.RESTAURANT_LATITUDE, 0.0)
        val restaurantLong = intent.getDoubleExtra(HomeFragment.RESTAURANT_LONGITUDE, 0.0)

        // Create a Uri for the destination using the restaurant's coordinates
        val uri = "https://www.google.com/maps/dir/?api=1&destination=$restaurantLat,$restaurantLong"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        // On get directions click, launch google maps Using the location the user has already given us
        getDirectionsButton.setOnClickListener {
            // Launch the Google Maps app
            intent.setPackage("com.google.android.apps.maps")

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            else {
                // Google Maps app is not installed, handle the situation accordingly
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Error")
                builder.setMessage("Google Maps is not installed on this device!")
                // Open Signup Activity again on dialog dismissal (click of OK)
                builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    // Method to get and set restaurant info passed from the Home fragment
    private fun getRestaurantInfoFromIntent() {
        // Retrieve the restaurant info from the intent
        restaurantId = intent.getStringExtra(HomeFragment.RESTAURANT_ID)!!
        restaurantName = intent.getStringExtra(HomeFragment.RESTAURANT_NAME)!!
        restaurantImg = intent.getStringExtra(HomeFragment.RESTAURANT_IMG)!!
        restaurantPrice = intent.getStringExtra(HomeFragment.RESTAURANT_PRICE)!!
        restaurantRating = intent.getStringExtra(HomeFragment.RESTAURANT_RATING)!!
        val displayAddressArray = intent.getStringArrayExtra(HomeFragment.RESTAURANT_ADDRESS)!!
        val displayAddress = displayAddressArray.toList()

        // Set restaurant info from intent to layout components
        collapsingToolbarTitle = findViewById(R.id.restaurantOverviewCollapsingToolBar)
        textViewRating = findViewById(R.id.overviewActivityRestaurantRating)
        textViewPrice = findViewById(R.id.overviewActivityPriceRange)
        textViewLocation = findViewById(R.id.overviewActivityLocation)
        imageViewImage = findViewById(R.id.overviewActivityRestaurantImage)

        collapsingToolbarTitle.title = restaurantName
        textViewPrice.text = restaurantPrice
        textViewRating.text = restaurantRating

        // Format address and set it to the TextView
        val formattedAddress = displayAddress?.joinToString("\n") ?: ""
        textViewLocation.text = formattedAddress

        // Load the image using Picasso, if there's no image, use the default one from the resources
        if (!restaurantImg.isNullOrEmpty()) {
            Picasso.get().load(restaurantImg).into(imageViewImage)
        }
        else {
            imageViewImage.setImageResource(R.drawable.food_pic4)
        }
    }

    // The updated list of reviews is handled here
    // Update the adapter with the new list of reviews
    private fun observeReviews() {
        reviewViewModel.observeReviewLiveData().observe(this, object :
            Observer<List<YelpReviews.YelpReview>> {
            override fun onChanged(value: List<YelpReviews.YelpReview>) {
                value?.let { reviews ->
                    reviewAdapter.updateReviews(reviews)
                }
            }
        })
    }

}