package com.example.fastconnect.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R

/**
 * MainActivity - Splash Screen / Entry Point.
 *
 * Displays the app logo and branding for 3 seconds,
 * then navigates to SignInActivity using Intent (Requirement F1).
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show splash screen for 3 seconds then navigate to Sign In
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
