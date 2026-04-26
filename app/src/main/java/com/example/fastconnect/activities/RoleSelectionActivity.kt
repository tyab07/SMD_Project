package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.google.android.material.card.MaterialCardView

class RoleSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val cardStudent = findViewById<MaterialCardView>(R.id.cardStudent)
        val cardAdmin = findViewById<MaterialCardView>(R.id.cardAdmin)
        val btnBack = findViewById<ImageView>(R.id.btnBackRole)

        cardStudent.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            intent.putExtra("ROLE_SELECTION", "user")
            startActivity(intent)
        }

        cardAdmin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            intent.putExtra("ROLE_SELECTION", "admin")
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
