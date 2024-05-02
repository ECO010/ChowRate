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

class SignUpActivity : AppCompatActivity() {

    // Declare activity components
    private lateinit var signInTextView: TextView
    private lateinit var signUpButton: Button
    private var auth = FirebaseAuth.getInstance()
    private lateinit var userNameEditText : EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var rePasswordEditText: EditText
    private var currentUser = auth.currentUser
    private lateinit var appDatabase : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Get an instance of the DB
        appDatabase = AppDatabase.getInstance(this)

        //Initialize the clickable components
        signInTextView = findViewById(R.id.signInTextView)
        signUpButton = findViewById(R.id.registerButton)
        userNameEditText = findViewById(R.id.editTextUsername)
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        rePasswordEditText = findViewById(R.id.editTextRePassword)
        signInTextView.isClickable = true

        // Should go to the home fragment when clicked
        // Should create the account
        signUpButton.setOnClickListener {
            // a user is already logged in, can't register
            if (currentUser != null ) {
                userLoggedInDialogDisplay()
            }
            // handling bad input scenarios
            else if (isBadInput()) {
                badInputDialogDisplay()
            }
            // Create the user and automatically sign them in
            else {
                auth.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString()).
                addOnCompleteListener(this) {
                    registerTask ->
                    if (registerTask.isSuccessful) {
                        val email = emailEditText.text.toString()
                        val password = passwordEditText.text.toString()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    // Successfully signed in after registration
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    lifecycleScope.launch { insertUserToDB() }
                                    finish()
                                } else {
                                    // Handle sign-in failure
                                    val exception = signInTask.exception
                                    val errorMessage = exception?.message ?: "Unknown error occurred"
                                    showErrorDialog(errorMessage)
                                }
                            }
                    }
                    else {
                        val exception = registerTask.exception
                        val errorMessage = exception?.message ?: "Unknown error occurred"
                        showErrorDialog(errorMessage)
                    }
                }
            }
        }

        // Should go to the sign in page when clicked
        signInTextView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Inserts the newly registered user into the DB
    private suspend fun insertUserToDB() {
        // Checks there's a user signed in
        if (currentUser != null) {
            val userId = currentUser!!.uid // User ID
            val userName = currentUser!!.email // Username

            // Checks that the user exists with the query function
            val userExists = appDatabase.userDao().getUserById(userId)

            // If no user with the id, create a new User object using the retrieved user details
            if (userExists == null)
            {
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
            userExistsDialogDisplay()
        }
    }

    // Dialog to display if a user already exists
    private fun userExistsDialogDisplay() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registration Failed")
        builder.setMessage("This user already exists in the database!")
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Dialog to display if a user is already signed in
    private fun userLoggedInDialogDisplay() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registration Failed")
        builder.setMessage("A user is already logged in, please sign out from the account and try again!")
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Dialog to display if there's been bad input while registering
    private fun badInputDialogDisplay() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registration Failed")
        builder.setMessage("Check that none of the text boxes are empty, also check that you have entered the same password twice")
        // Open Signup Activity again on dialog dismissal (click of OK)
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Dialog to display if there's been any other error
    private fun showErrorDialog(errorMessage: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registration Failed")
        builder.setMessage(errorMessage)
        builder.setPositiveButton(getString(R.string.dialog_button)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Checks if edit boxes are empty or if the password edit boxes don't match
    private fun isBadInput(): Boolean {
        val userName = userNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val rePassword = rePasswordEditText.text.toString()

        return when {
            userName.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty() -> true
            password != rePassword -> true
            else -> false
        }
    }
}