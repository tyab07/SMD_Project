package com.example.fastconnect.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val btnAddSociety = findViewById<Button>(R.id.btnAddSociety)
        val btnAddAnnouncement = findViewById<Button>(R.id.btnAddAnnouncement)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnAddSociety.setOnClickListener {
            startActivity(Intent(this, AddSocietyActivity::class.java))
        }

        btnAddAnnouncement.setOnClickListener {
            startActivity(Intent(this, AddAnnouncementActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("FastConnectPrefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
