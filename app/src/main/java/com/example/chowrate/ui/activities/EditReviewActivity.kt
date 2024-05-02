package com.example.chowrate.ui.activities

import ReviewAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditReviewActivity : AppCompatActivity() {

    // Declaring the activity components
    private lateinit var reviewText: String
    private lateinit var reviewId: String
    private lateinit var restaurantIdAlias: String
    private var rating = 0
    private lateinit var ratingBar: RatingBar
    private lateinit var writeReviewButton: Button
    private lateinit var characterCountTextView: TextView
    private lateinit var editTextText: EditText
    private val appDatabase = AppDatabase.getInstance(this)
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_review)

        // Get review details from selected review item
        reviewText = intent.getStringExtra(ReviewAdapter.REVIEW_TEXT)!!
        rating = intent.getIntExtra(ReviewAdapter.REVIEW_RATINGS, 0)
        reviewId = intent.getStringExtra(ReviewAdapter.REVIEW_ID)!!
        restaurantIdAlias = intent.getStringExtra(ReviewAdapter.RESTAURANT_ID_ALIAS)!!

        // Initialize the clickable components and other components
        editTextText = findViewById(R.id.editTextExperience)
        editTextText.setText(reviewText)
        ratingBar = findViewById(R.id.ratingBar)
        ratingBar.rating = rating.toFloat()

        characterCountTextView = findViewById(R.id.characterCountTextView)
        writeReviewButton = findViewById(R.id.writeReviewButton)

        // Should submit the review and show a dialog to confirm
        writeReviewButton.setOnClickListener {
            // submit the review
            submitReview(editTextText.text.toString(),ratingBar.rating.toInt(),restaurantIdAlias,getCurrentTimeAsString())
            // shows the dialog and navigates
            showDialogAndNavigateToHome()
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

    // Get the device's current time as a string
    private fun getCurrentTimeAsString(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = Date(currentTimeMillis)
        return dateFormat.format(currentTime)
    }

    // a method to submit the edited review
    private fun submitReview(text: String, rating: Int, restaurantAlias: String, timeCreated: String) {
        val userId = auth.currentUser!!.uid
        val userName = auth.currentUser!!.email
        val currentUser = YelpReviews.YelpReview.User(
            id = userId,
            profileUrl = null, // hardcoded as this is not yet implemented
            imageUrl = null, // hardcoded as this is not yet implemented
            name = userName ?: ""
        )
        val review = YelpReviews.YelpReview(reviewId, "", restaurantAlias, text, rating, timeCreated, currentUser)

        // Use Dispatchers.IO for database operation as we're calling this in the main thread
        lifecycleScope.launch(Dispatchers.IO) {
            appDatabase.yelpReviewDao().updateReview(review)
            Log.d("TESTSuccessfulReview", review.toString())
        }
    }

    // Method to display a dialog and navigate to the home fragment, Only implemented for a successful update at the moment
    private fun showDialogAndNavigateToHome() {
        val builder = AlertDialog.Builder(this@EditReviewActivity)
        builder.setTitle("Review Update Complete")
        builder.setMessage("Thanks for updating your experience. Click OK to return to the home page and continue exploring restaurants")
        // Dismiss and navigate to the home page
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            // navigate to the home fragment
            val intent = Intent(this@EditReviewActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}