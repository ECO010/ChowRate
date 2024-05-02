package com.example.chowrate.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chowrate.R
import com.example.chowrate.data.model.YelpReviews
import com.example.chowrate.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    // Declare the clickable components
    private lateinit var signUpTextView: TextView
    private lateinit var signInButton: Button
    private lateinit var userNameTextBox : EditText
    private lateinit var passwordTextBox : EditText
    private var auth = FirebaseAuth.getInstance()
    var currentUser = auth.currentUser
    private lateinit var appDatabase : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        appDatabase = AppDatabase.getInstance(this)

        //Initialize the clickable components
        signUpTextView = findViewById(R.id.signUpTextView)
        signInButton = findViewById(R.id.signInButton2)
        userNameTextBox = findViewById(R.id.editTextUsernameOrEmail)
        passwordTextBox = findViewById(R.id.editTextPassword1)
        signUpTextView.isClickable = true


        // Should go to the homepage when clicked
        // Should load user account settings
        signInButton.setOnClickListener {
            auth.signInWithEmailAndPassword(
                userNameTextBox.text.toString(),
                passwordTextBox.text.toString()).addOnCompleteListener(this) {
                    task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)

                        // Launch Coroutine to insert user to DB
                        lifecycleScope.launch { insertUserToDB() }
                        finish()
                    }
                    else {
                        dialogDisplay()
                    }
                }
        }

        // Should go to the sign up page when clicked
        signUpTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // TO-DO: add user to DB if they don't exist and call it when registration has been successful in the onCreate
    private suspend fun insertUserToDB() {
        if (currentUser != null) {
            val userId = currentUser!!.uid // User ID
            val userName = currentUser!!.email // Username

            // Check the user exists with query function
            val userExists = appDatabase.userDao().getUserById(userId)

            // No user with the id
            if (userExists == null)
            {
                // Create a new User object using the retrieved user details
                val user = YelpReviews.YelpReview.User(
                    id = userId,
                    profileUrl = null,
                    imageUrl = null,
                    name = userName ?: ""
                )
                // Insert into Room database
                appDatabase.userDao().addUser(user)
            }
        }
        else {
            return
            userExistsDialogDisplay()
        }
    }

    // Function that displays a dialog if there has been a problem signing in
    private fun dialogDisplay() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login Failed")
        builder.setMessage("The account you are trying to sign into does not exist or the password is wrong, please try again!")
        // Open Login Activity again on dialog dismissal (click of OK)
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Function that displays a dialog if there has been a problem signing in
    private fun userExistsDialogDisplay() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registration Failed")
        builder.setMessage("This user already exists in the database!")
        // Open Signup Activity again on dialog dismissal (click of OK)
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}