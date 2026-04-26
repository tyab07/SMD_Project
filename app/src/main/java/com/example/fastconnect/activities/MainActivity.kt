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

        // Show splash screen for 3 seconds then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("FastConnectPrefs", MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)

            val intent = if (isLoggedIn) {
                val userRole = prefs.getString("USER_ROLE", "user")
                if (userRole == "admin") {
                    Intent(this, AdminDashboardActivity::class.java)
                } else {
                    Intent(this, DashboardActivity::class.java).apply {
                        putExtra("USER_ID", prefs.getLong("USER_ID", -1L))
                        putExtra("USER_NAME", prefs.getString("USER_NAME", ""))
                        putExtra("USER_ROLE", userRole)
                    }
                }
            } else {
                Intent(this, RoleSelectionActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}
