import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.chowrate.R
import com.example.chowrate.data.model.YelpReviews // Replace with your Reviews data class package
import com.example.chowrate.database.AppDatabase
import com.example.chowrate.ui.activities.EditReviewActivity
import com.example.chowrate.ui.activities.MainActivity
import com.example.chowrate.ui.activities.RestaurantOverviewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReviewAdapter(private var yelpReviewList: List<YelpReviews.YelpReview>,
    private val context: Context) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // Initialize authentication for
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser

    // initialize companion objects passed to the edit review activity page
    companion object {
        const val REVIEW_TEXT = "com.example.chowrate.ui.datamodel.ReviewText"
        const val REVIEW_ID = "com.example.chowrate.ui.datamodel.ReviewID"
        const val REVIEW_RATINGS = "com.example.chowrate.ui.datamodel.ReviewRatings"
        const val RESTAURANT_ID_ALIAS = "com.example.chowrate.ui.datamodel.RestaurantIdAlias"
    }

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Initializing review item components
        val reviewText: TextView = itemView.findViewById(R.id.reviewText)
        val reviewUserName: TextView = itemView.findViewById(R.id.reviewUserName)
        val reviewRating: RatingBar = itemView.findViewById(R.id.reviewRating)
        val reviewDate: TextView = itemView.findViewById(R.id.reviewDate)
        val reviewImage: ImageView = itemView.findViewById(R.id.reviewImage)

        fun bind(yelpReview: YelpReviews.YelpReview) {
            reviewText.text = yelpReview.text
            reviewUserName.text = yelpReview.user.name
            reviewRating.rating = yelpReview.rating.toFloat()
            reviewDate.text = yelpReview.timeCreated

            // There is no review image variable so I can either load the user's image or just skip
            // I decided to skip but use a default image for reviews not coming from yelp
            if (!yelpReview.url.isNullOrEmpty()) {
                Picasso.get().load(yelpReview.url).into(reviewImage)
            }
            else {
                // Set a default image if no user image is available (skip action)
                reviewImage.setImageResource(R.drawable.food_critic_icon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val currentItem = yelpReviewList[position]

        // Check if the current user is the one who posted the review item
        // This determines the visibility of the manage review button
        val isCurrentUserReviewer = currentItem.user.id == currentUser?.uid

        if (isCurrentUserReviewer) {
            holder.itemView.findViewById<FloatingActionButton>(R.id.manageReviewButton).visibility = View.VISIBLE

            // On click of the manage review button
            holder.itemView.findViewById<FloatingActionButton>(R.id.manageReviewButton).setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Manage Review")
                builder.setMessage("Do you want to delete or edit the review?")

                // Open Edit Review Activity on clicking "Edit" button
                builder.setNeutralButton("Edit") { dialog, _ ->
                    dialog.dismiss()

                    // Create an intent to start EditReviewActivity
                    val editReviewIntent = Intent(context, EditReviewActivity::class.java)

                    // Pass details of the current review item to the EditReviewActivity
                    editReviewIntent.putExtra(REVIEW_TEXT, currentItem.text)
                    editReviewIntent.putExtra(REVIEW_RATINGS, currentItem.rating)
                    editReviewIntent.putExtra(REVIEW_ID, currentItem.id)
                    editReviewIntent.putExtra(RESTAURANT_ID_ALIAS, currentItem.restaurantAlias)

                    // Testing in Logcat to make sure the right data was being retrieved
                    Log.d("TESTReviewText", currentItem.text)
                    Log.d("TESTRating", currentItem.rating.toString())
                    Log.d("TESTReviewId", currentItem.id)
                    Log.d("TESTRestaurantAlias", currentItem.restaurantAlias)

                    // Start the EditReviewActivity
                    context.startActivity(editReviewIntent)
                    //context.finish()
                }

                // Delete the review on clicking "Delete" button
                builder.setPositiveButton("Delete") { dialog, _ ->
                    dialog.dismiss()

                    // Delete the current review from the database
                    // Not optimal to use GlobalScope but if it's just one review to delete I guess it's fine
                    val appDatabase = AppDatabase.getInstance(context)
                    GlobalScope.launch {
                        appDatabase.yelpReviewDao().deleteReview(currentItem)
                    }

                    // Display a dialog confirming the successful deletion
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Review Deleted")
                        .setMessage("The review has been successfully deleted. Click OK to return to the home page and continue exploring restaurants.")
                        .setPositiveButton("OK") { dialog, _ ->

                            dialog.dismiss()

                            // Start the main activity again
                            val mainActivityIntent = Intent(context, MainActivity::class.java)
                            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(mainActivityIntent)
                            (context as Activity).finish()
                        }
                        .create()
                    alertDialog.show()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        else {
            holder.itemView.findViewById<FloatingActionButton>(R.id.manageReviewButton).visibility = View.GONE
        }
        holder.bind(currentItem)
    }

    fun updateReviews(yelpReviews: List<YelpReviews.YelpReview>) {
        yelpReviewList = yelpReviews
        notifyDataSetChanged()
    }

    override fun getItemCount() = yelpReviewList.size
}
