package com.example.fastconnect.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fastconnect.R
import com.example.fastconnect.fragments.BookmarksFragment
import com.example.fastconnect.fragments.CoursesFragment
import com.example.fastconnect.fragments.HomeFragment
import com.example.fastconnect.fragments.NewsFragment
import com.example.fastconnect.fragments.ProfileFragment
import com.example.fastconnect.fragments.SocietiesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * DashboardActivity - Main container/coordinator for all Fragments.
 *
 * This Activity acts ONLY as a container (Modular UI Design constraint).
 * All primary UI content is implemented in Fragments.
 *
 * Updated for Assignment #03:
 * - Added NewsFragment for REST API data display (F1)
 * - Added BookmarksFragment for SQLite CRUD operations (F3)
 */
class DashboardActivity : AppCompatActivity() {

    // User data received via Intent Extras
    private var userName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Receive user data from SignInActivity via Intent Extras
        userName = intent.getStringExtra("USER_NAME") ?: "Student"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        Toast.makeText(this, "Welcome, $userName!", Toast.LENGTH_SHORT).show()

        // Setup BottomNavigationView for fragment switching
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment.newInstance(userName, userEmail))
                    true
                }
                R.id.nav_news -> {
                    // F1: Load NewsFragment for REST API data display
                    loadFragment(NewsFragment.newInstance())
                    true
                }
                R.id.nav_courses -> {
                    loadFragment(CoursesFragment.newInstance())
                    true
                }
                R.id.nav_societies -> {
                    loadFragment(SocietiesFragment.newInstance())
                    true
                }
                R.id.nav_bookmarks -> {
                    // F3: Load BookmarksFragment for SQLite CRUD
                    loadFragment(BookmarksFragment.newInstance())
                    true
                }
                R.id.nav_profile -> {
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
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Handle back press for fragment back stack.
     * If a child fragment (e.g., CourseDetail, AddEditBookmark) is showing, pop back.
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
