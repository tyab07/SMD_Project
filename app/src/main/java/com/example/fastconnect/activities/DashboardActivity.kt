package com.example.fastconnect.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fastconnect.R
import com.example.fastconnect.fragments.CoursesFragment
import com.example.fastconnect.fragments.HomeFragment
import com.example.fastconnect.fragments.ProfileFragment
import com.example.fastconnect.fragments.SocietiesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * DashboardActivity - Main container/coordinator for all Fragments.
 *
 * This Activity acts ONLY as a container (Modular UI Design constraint).
 * All primary UI content is implemented in Fragments.
 *
 * Requirement F1: Receives user data from SignInActivity via Intent Extras,
 *   then passes it to Fragments via Bundles.
 * Requirement F4: Switches between Fragments using fragment transactions
 *   without restarting the Activity.
 */
class DashboardActivity : AppCompatActivity() {

    // User data received via Intent Extras (F1)
    private var userName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // F1: Receive user data from SignInActivity via Intent Extras
        userName = intent.getStringExtra("USER_NAME") ?: "Student"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        Toast.makeText(this, "Welcome, $userName!", Toast.LENGTH_SHORT).show()

        // Setup BottomNavigationView for fragment switching (F4)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // F1: Pass user data to HomeFragment via Bundle
                    loadFragment(HomeFragment.newInstance(userName, userEmail))
                    true
                }
                R.id.nav_courses -> {
                    // F4: Switch to CoursesFragment without restarting activity
                    loadFragment(CoursesFragment.newInstance())
                    true
                }
                R.id.nav_societies -> {
                    // F4: Switch to SocietiesFragment without restarting activity
                    loadFragment(SocietiesFragment.newInstance())
                    true
                }
                R.id.nav_profile -> {
                    // F1: Pass user data to ProfileFragment via Bundle
                    loadFragment(ProfileFragment.newInstance(userName, userEmail))
                    true
                }
                else -> false
            }
        }

        // Load HomeFragment by default on first launch
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    /**
     * Loads a Fragment into the container using fragment transactions.
     *
     * Requirement F4: Fragment transactions to switch between fragments
     * without restarting the activity.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Handle back press for fragment back stack (F4).
     * If CourseDetailFragment is showing, pop back to CoursesFragment.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
