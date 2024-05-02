package com.example.chowrate.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.chowrate.R
import com.example.chowrate.data.model.YelpReviews
import com.example.chowrate.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WriteReviewActivity : AppCompatActivity() {

    // Declaring activity components
    private lateinit var editTextText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var writeReviewButton: Button
    private lateinit var characterCountTextView: TextView
    private lateinit var autoCompleteLocation : AutoCompleteTextView
    private val appDatabase = AppDatabase.getInstance(this)
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        // Initialize clickable components
        editTextText = findViewById(R.id.editTextExperience)
        characterCountTextView = findViewById(R.id.characterCountTextView)
        autoCompleteLocation = findViewById(R.id.autoCompleteLocation)
        ratingBar = findViewById(R.id.ratingBar)
        writeReviewButton = findViewById(R.id.writeReviewButton)


        // Create an array adapter with the restaurant names fetched from the query function
        lifecycleScope.launch {
            val restaurantNames = withContext(Dispatchers.IO) {
                appDatabase.restaurantDao().getAllRestaurantNames()
            }
            val adapter = ArrayAdapter(this@WriteReviewActivity, android.R.layout.simple_dropdown_item_1line, restaurantNames)

            // set the location auto complete text view to use this adapter
            autoCompleteLocation.setAdapter(adapter)
        }

        // Dropdown Item Click Listener:
        autoCompleteLocation.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedRestaurant = parent.getItemAtPosition(position) as String

            // Fetch the restaurant alias(id) based on the selected name
            lifecycleScope.launch {
                val restaurantAlias = withContext(Dispatchers.IO) {
                    appDatabase.restaurantDao().getRestaurantAliasFromName(selectedRestaurant)
                }
                // Store the obtained restaurant alias to use in the submit review method
                val selectedAlias = restaurantAlias

                // Should submit the review and show a dialog to confirm
                writeReviewButton.setOnClickListener {
                    // submit the review
                    submitReview(editTextText.text.toString(),ratingBar.rating.toInt(),selectedAlias,getCurrentTimeAsString())
                    // shows the dialog and navigates
                    showDialogAndNavigateToHome()
                }
            }
        }

        // Monitor the text input
        // TO-DO: Add warning when character limit is reached
        editTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used in this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used in this implementation
            }

            override fun afterTextChanged(s: Editable?) {
                val characterCount = s?.length ?: 0
                val maxCharacterLimit = 100

                // Update the character count TextView
                characterCountTextView.text = "$characterCount/$maxCharacterLimit"
            }
        })
    }

    // Method to generate a random string for the review ID
    private fun generateRandomString(): String {
        return UUID.randomUUID().toString()
    }

    // Get the device's current time as a string
    private fun getCurrentTimeAsString(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = Date(currentTimeMillis)
        return dateFormat.format(currentTime)
    }

    // a method to submit the written review
    private fun submitReview(text: String, rating: Int, restaurantAlias: String, timeCreated: String) {
        val userId = auth.currentUser!!.uid
        val userName = auth.currentUser!!.email
        val currentUser = YelpReviews.YelpReview.User(
            id = userId,
            profileUrl = null, // hardcoded as this is not yet implemented
            imageUrl = null, // hardcoded as this is not yet implemented
            name = userName ?: ""
        )
        val randomIdString = generateRandomString()
        val review = YelpReviews.YelpReview(randomIdString, "", restaurantAlias, text, rating, timeCreated, currentUser)

        // Add the review to the DB
        lifecycleScope.launch(Dispatchers.IO) {
            appDatabase.yelpReviewDao().addNewReview(review)
            Log.d("TESTSuccessfulReview", review.toString())
        }
    }

    // Method to display a dialog and navigate to the home fragment, Only implemented for a successful review at the moment
    private fun showDialogAndNavigateToHome() {
        val builder = AlertDialog.Builder(this@WriteReviewActivity)
        builder.setTitle("Review Complete")
        builder.setMessage("Thanks for sharing your experience. Click OK to return to the home page and continue exploring restaurants")
        // Dismiss and navigate to the home page
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            // navigate to the home fragment
            val intent = Intent(this@WriteReviewActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}