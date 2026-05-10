package com.example.fastconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.example.fastconnect.firebase.FirebaseHelper

/**
 * AddSocietyActivity — Admin screen to create a new society.
 *
 * Updated for Assignment#04: Writes to Firebase Realtime Database
 * at /societies/{pushId} instead of local SQLite.
 */
class AddSocietyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_society)

        val etName = findViewById<EditText>(R.id.etSocietyName)
        val etDesc = findViewById<EditText>(R.id.etSocietyDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitSociety)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating..."

            // F2: Write to Firebase Realtime Database
            FirebaseHelper.addSociety(name, desc) { success ->
                if (success) {
                    Toast.makeText(this, "Society created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit"
                    Toast.makeText(this, "Failed to create society. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
