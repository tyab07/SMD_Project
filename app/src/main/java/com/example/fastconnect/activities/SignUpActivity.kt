package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.example.fastconnect.firebase.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

/**
 * SignUpActivity — Handles new user registration via Firebase Authentication (F1).
 *
 * Uses FirebaseAuth.createUserWithEmailAndPassword() to register,
 * then saves the user profile to Firebase Realtime Database at /users/{uid}.
 * Also creates default bookmark folders for the new user.
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailSignUp)
        val passwordInput = findViewById<EditText>(R.id.passwordSignUp)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        // F1: Register via Firebase Auth + save profile to Realtime DB
        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Please enter your name"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailInput.error = "Please enter your email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordInput.error = "Please enter a password"
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordInput.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            registerBtn.isEnabled = false
            registerBtn.text = "Creating account..."

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Save user profile to Firebase Realtime Database
                        FirebaseHelper.saveUserProfile(
                            uid = uid,
                            name = name,
                            email = email,
                            role = "user"
                        ) { success ->
                            if (success) {
                                // Create default bookmark folders for the new user
                                FirebaseHelper.createDefaultFolders(uid)

                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                                // Sign out so user can sign in explicitly
                                auth.signOut()

                                // Navigate back to SignInActivity with registration data
                                val intent = Intent(this, SignInActivity::class.java)
                                intent.putExtra("REGISTERED_NAME", name)
                                intent.putExtra("REGISTERED_EMAIL", email)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                startActivity(intent)
                                finish()
                            } else {
                                registerBtn.isEnabled = true
                                registerBtn.text = "Register"
                                Toast.makeText(this, "Failed to save profile. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        registerBtn.isEnabled = true
                        registerBtn.text = "Register"
                        val errorMsg = task.exception?.message ?: "Registration failed"
                        Toast.makeText(this, "Registration failed: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
