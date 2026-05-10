package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R

/**
 * MainActivity — Splash Screen / Entry Point.
 * 
 * Simplified to bypass any Firebase initialization crashes during startup.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val intent = Intent(this, RoleSelectionActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 2000)
    }
}
