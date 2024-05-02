package com.example.chowrate.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.chowrate.R
import com.example.chowrate.ui.activities.LandingPageActivity
import com.google.firebase.auth.FirebaseAuth


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    // Declare Fragment components
    private lateinit var signOutButton: Button
    private lateinit var profileAvatar: ImageView
    private lateinit var signInConstraintLayout: ConstraintLayout
    private lateinit var profileConstraintLayout: ConstraintLayout
    private lateinit var reviewInsightsConstraintLayout: ConstraintLayout
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize layout components
        signOutButton = view.findViewById(R.id.signOutButton)
        signInConstraintLayout = view.findViewById(R.id.signInConstraintLayout)
        profileAvatar = view.findViewById(R.id.profileAvatar)
        profileConstraintLayout = view.findViewById(R.id.profileConstraintLayout)
        reviewInsightsConstraintLayout = view.findViewById(R.id.reviewInsightsConstraintLayout)

        changeLayoutBasedOnUser()

        // Should go to the sign in page when clicked
        // Should log the current user out
        signOutButton.setOnClickListener {
            currentUser
            auth.signOut()
            val intent = Intent(requireActivity(), LandingPageActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return view
    }

    // A method that displays or hides components based on the user status (registered or guest)
    private fun changeLayoutBasedOnUser () {
        if (currentUser != null) {
            signInConstraintLayout.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            profileAvatar.visibility = View.VISIBLE
            profileConstraintLayout.visibility = View.VISIBLE
            reviewInsightsConstraintLayout.visibility = View.VISIBLE
        }
        else {
            signInConstraintLayout.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
            profileAvatar.visibility = View.GONE
            profileConstraintLayout.visibility = View.GONE
            reviewInsightsConstraintLayout.visibility = View.GONE
        }
    }
}