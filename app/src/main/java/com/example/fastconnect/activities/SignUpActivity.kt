package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R

/**
 * SignUpActivity - Handles new user registration.
 *
 * Requirement F1: Returns registration data (name, email) to SignInActivity
 * via Intent Extras, demonstrating inter-Activity data communication.
 */
class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailSignUp)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        // F1: Register and pass data back to SignInActivity via Intent
        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Please enter your name"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailInput.error = "Please enter your email"
                return@setOnClickListener
            }

            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

            // F1: Pass registration data back to SignInActivity via Intent Extras
            val intent = Intent(this, SignInActivity::class.java)
            intent.putExtra("REGISTERED_NAME", name)
            intent.putExtra("REGISTERED_EMAIL", email)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
