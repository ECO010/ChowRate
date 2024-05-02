package com.example.chowrate.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chowrate.R
import com.google.firebase.auth.FirebaseAuth

class LandingPageActivity : AppCompatActivity() {

    // Declare the clickable components
    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button
    private lateinit var continueAsGuestButton: Button
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser

    // Initializing random location permission code
    private val locationPermissionCode = 123

    // Checks if there's a user signed in
    // goes straight to the home page if there's a user signed in
    private fun checkIfUserSignedIn() {
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        checkIfUserSignedIn()

        // Initialize the clickable components
        signUpButton = findViewById(R.id.signUpButton)
        signInButton = findViewById(R.id.signInButton)
        continueAsGuestButton = findViewById(R.id.continueAsGuestButton)

        // Registers the permissions callback, which handles the user's response to the system permissions dialog.
        // Saves the return value, an instance of ActivityResultLauncher.
        // Defines the permission launcher
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // If granted recreate the activity and wait for the user's input
                    this.recreate()
                }
                else {
                    // Build a dialog to explain the importance of the permissions for UX
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.permissions_dialog_title))
                    builder.setMessage(getString(R.string.location_dialog_message))
                    // Request Permission Pop up again on dialog dismissal (click of OK)
                    builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            locationPermissionCode
                        )
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
            }

        //If location permission is explicitly denied, show dialog pop up explaining to the user the need for this
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Check if location permission is explicitly granted
        else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If it is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        // Permission is already granted, proceed to the desired activity on click of buttons
        else {
            // On click of Continue as Guest, Location Permissions are checked
            // There is a prompt for the user to allow locations
            continueAsGuestButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            // Should go to the sign in activity when clicked
            signInButton.setOnClickListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
            // Should go to the sign up activity when clicked
            signUpButton.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}