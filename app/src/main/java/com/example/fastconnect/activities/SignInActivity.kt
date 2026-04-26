package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * SignInActivity - Handles user login.
 *
 * Requirement F1: Passes user data (email, name) to DashboardActivity via Intent Extras.
 * Data flows: SignInActivity → Intent.putExtra() → DashboardActivity → Bundle → Fragments
 */
class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val toSignUp = findViewById<TextView>(R.id.toSignUp)
        
        val roleSelection = intent.getStringExtra("ROLE_SELECTION") ?: "user"
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        if (roleSelection == "admin") {
            tvSubtitle.text = "Sign in to Administrator Dashboard"
            toSignUp.visibility = android.view.View.GONE
            findViewById<TextView>(R.id.tvWelcome).text = "Admin Access"
        } else {
            tvSubtitle.text = "Sign in to continue to FAST Hub"
        }

        // Check if returning from SignUpActivity with registration data
        // F1: Receive data back from SignUpActivity
        val registeredName = intent.getStringExtra("REGISTERED_NAME")
        val registeredEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            emailInput.setText(registeredEmail)
            Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
        }

        // F1: Login
        btnLogin.setOnClickListener {
            val email = emailInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""

            if (email.isEmpty()) {
                emailInput.error = "Please enter your email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordInput.error = "Please enter your password"
                return@setOnClickListener
            }

            // Check hardcoded admin if role is admin
            if (roleSelection == "admin") {
                if (email == "tayyab@gmail.com" && password == "123") {
                    val prefs = getSharedPreferences("FastConnectPrefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putBoolean("IS_LOGGED_IN", true)
                        putLong("USER_ID", 0L)
                        putString("USER_NAME", "Admin Tayyab")
                        putString("USER_ROLE", "admin")
                        apply()
                    }
    
                    Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Administrator Credentials.", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            // Regular user login via DB
            val dbHelper = com.example.fastconnect.db.FastConnectDbHelper(this)
            val user = dbHelper.checkUserLogin(email, password)

            if (user == null) {
                // Not found or incorrect password
                val existingAccount = dbHelper.getUserByEmail(email)
                if (existingAccount == null) {
                    Toast.makeText(this, "User not found. Please sign up.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            // Save session
            val prefs = getSharedPreferences("FastConnectPrefs", MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("IS_LOGGED_IN", true)
                putLong("USER_ID", user.id)
                putString("USER_NAME", user.name)
                putString("USER_ROLE", user.role)
                apply()
            }

            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

            // F1: Pass data to DashboardActivity using Intent Extras
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            intent.putExtra("USER_NAME", user.name)
            intent.putExtra("USER_EMAIL", user.email)
            intent.putExtra("USER_ROLE", user.role)
            startActivity(intent)
            finish()
        }

        // Navigate to Sign Up
        toSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
