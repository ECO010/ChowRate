package com.example.chowrate.data.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chowrate.R
import com.squareup.picasso.Picasso

class RestaurantAdapter(private var restaurantList: List<Restaurants.Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize components of restaurant item
        val restaurantPriceRange: TextView = itemView.findViewById(R.id.priceRange)
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        val restaurantRating: RatingBar = itemView.findViewById(R.id.restaurantRating)
        val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)

        fun bind(restaurant: Restaurants.Restaurant) {
            restaurantName.text = restaurant.name
            restaurantRating.rating = restaurant.rating
            restaurantPriceRange.text = restaurant.price

            // Load the image using Picasso, if there's no image, use the default one from the resources
            if (!restaurant.image_url.isNullOrEmpty()) {
                Picasso.get().load(restaurant.image_url).into(restaurantImage)
            }
            else {
                restaurantImage.setImageResource(R.drawable.food_pic4)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.restaurant_item, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val currentItem = restaurantList[position]

        // Set data to views in restaurant_item.xml
        holder.bind(currentItem)

        // Handle item click listener if needed
        holder.itemView.setOnClickListener {
            // Handle item click, navigate to details, etc.
            onItemClickListener?.invoke(position)
        }
    }

    fun updateRestaurants(restaurants: List<Restaurants.Restaurant>) {
        restaurantList = restaurants
        notifyDataSetChanged()
    }

    override fun getItemCount() = restaurantList.size

}
