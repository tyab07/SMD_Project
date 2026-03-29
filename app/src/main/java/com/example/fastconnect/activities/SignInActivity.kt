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
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val toSignUp = findViewById<TextView>(R.id.toSignUp)

        // Check if returning from SignUpActivity with registration data
        // F1: Receive data back from SignUpActivity
        val registeredName = intent.getStringExtra("REGISTERED_NAME")
        val registeredEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            emailInput.setText(registeredEmail)
            Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
        }

        // F1: Login — Pass user data to DashboardActivity via Intent Extras
        btnLogin.setOnClickListener {
            val email = emailInput.text?.toString()?.trim() ?: ""

            if (email.isEmpty()) {
                emailInput.error = "Please enter your email"
                return@setOnClickListener
            }

            // Extract username from email (part before @)
            val userName = if (email.contains("@")) {
                email.substringBefore("@").replaceFirstChar { it.uppercase() }
            } else {
                email.replaceFirstChar { it.uppercase() }
            }

            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

            // F1: Pass data to DashboardActivity using Intent Extras
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_NAME", registeredName ?: userName)
            intent.putExtra("USER_EMAIL", email)
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
