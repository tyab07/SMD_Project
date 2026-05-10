package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.example.fastconnect.firebase.FirebaseHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

/**
 * SignInActivity — Handles user login via Firebase Authentication (F1).
 */
class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val toSignUp = findViewById<TextView>(R.id.toSignUp)

        val roleSelection = intent.getStringExtra("ROLE_SELECTION") ?: "user"
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        
        if (roleSelection == "admin") {
            tvSubtitle.text = "Sign in to Administrator Dashboard"
            toSignUp.visibility = View.GONE
            findViewById<TextView>(R.id.tvWelcome).text = "Admin Access"
        } else {
            tvSubtitle.text = "Sign in to continue to FAST Hub"
        }

        val registeredEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            emailInput.setText(registeredEmail)
        }

        btnLogin.setOnClickListener {
            val email = emailInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Signing in..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser ?: return@addOnCompleteListener
                        handleSuccessfulLogin(user.uid, user.displayName ?: "", email, roleSelection)
                    } else {
                        btnLogin.isEnabled = true
                        btnLogin.text = "LOGIN"
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        toSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun handleSuccessfulLogin(uid: String, displayName: String, email: String, roleSelection: String) {
        FirebaseHelper.getUserProfile(uid) { user ->
            val userName = user?.name ?: displayName
            val userRole = user?.role ?: "user"

            val prefs = getSharedPreferences("FastConnectPrefs", MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("IS_LOGGED_IN", true)
                putString("USER_UID", uid)
                putString("USER_NAME", userName)
                putString("USER_EMAIL", email)
                putString("USER_ROLE", userRole)
                apply()
            }

            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

            // Check if user is logging into the correct portal
            if (roleSelection == "admin" && userRole != "admin") {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this@SignInActivity, "Access Denied: You are not an Admin", Toast.LENGTH_SHORT).show()
                val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                return@getUserProfile
            } else if (roleSelection == "user" && userRole == "admin") {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this@SignInActivity, "Access Denied: Please use the Administrative Portal", Toast.LENGTH_SHORT).show()
                val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                return@getUserProfile
            }

            // Navigate and CLEAR the backstack to prevent the "Role Selection loop"
            val targetActivity = if (userRole == "admin") {
                AdminDashboardActivity::class.java
            } else {
                DashboardActivity::class.java
            }

            val intent = Intent(this, targetActivity).apply {
                putExtra("USER_UID", uid)
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", email)
                putExtra("USER_ROLE", userRole)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
