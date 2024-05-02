package com.example.chowrate.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.chowrate.R
import com.example.chowrate.ui.activities.SignUpActivity
import com.example.chowrate.ui.activities.WriteReviewActivity
import com.google.firebase.auth.FirebaseAuth

/*// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/**
 * A simple [Fragment] subclass.
 * Use the [ReviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReviewFragment : Fragment() {
/*    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null*/

    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser
    private lateinit var signInButton: Button
    private lateinit var createAnAccountButton: Button
    private lateinit var uploadPhotoButton: Button
    private lateinit var writeReviewButton: Button
    private lateinit var orTextView: TextView
    private lateinit var startReviewTextView: TextView
    private lateinit var channelTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_review, container, false)

        // Initialize layout components
        signInButton = view.findViewById(R.id.reviewFragSignInBtn)
        createAnAccountButton = view.findViewById(R.id.reviewFragCreateAccountBtn)
        uploadPhotoButton = view.findViewById(R.id.uploadAPhotoBtn)
        writeReviewButton = view.findViewById(R.id.writeAReviewBtn)
        orTextView = view.findViewById(R.id.OrTextView)
        startReviewTextView = view.findViewById(R.id.start_a_reviewTextView)
        channelTextView = view.findViewById(R.id.channel_food_criticTextView)

        changeLayoutBasedOnUser()

        // TODO: Set OnClick Listeners for all the buttons and make them do something
        // TODO: Start with the review buttons because they are the most important
        // Should go to the write review page
        writeReviewButton.setOnClickListener {
            val intent = Intent(requireContext(), WriteReviewActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun changeLayoutBasedOnUser () {
        if (currentUser != null) {
            orTextView.visibility = View.GONE
            channelTextView.visibility = View.GONE
            createAnAccountButton.visibility = View.GONE
            signInButton.visibility = View.GONE
            uploadPhotoButton.visibility = View.VISIBLE
            writeReviewButton.visibility = View.VISIBLE
            startReviewTextView.visibility = View.VISIBLE
        }
        else {
            orTextView.visibility = View.VISIBLE
            channelTextView.visibility = View.VISIBLE
            createAnAccountButton.visibility = View.VISIBLE
            signInButton.visibility = View.VISIBLE
            uploadPhotoButton.visibility = View.GONE
            writeReviewButton.visibility = View.GONE
            startReviewTextView.visibility = View.GONE
        }
    }

/*    companion object {
        *//**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReviewFragment.
         *//*
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}