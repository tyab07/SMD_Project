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
import com.google.firebase.auth.FirebaseAuth

/**
 * DashboardActivity — Main container/coordinator for all Fragments.
 *
 * Updated for Assignment#04: Uses Firebase Auth for user data.
 * All primary UI content is implemented in Fragments (Modular UI Design).
 */
class DashboardActivity : AppCompatActivity() {

    private var userName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Get user data from Intent Extras or Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        userName = intent.getStringExtra("USER_NAME") ?: currentUser?.displayName ?: "Student"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: currentUser?.email ?: ""

        Toast.makeText(this, "Welcome, $userName!", Toast.LENGTH_SHORT).show()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment.newInstance(userName, userEmail))
                    true
                }
                R.id.nav_news -> {
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
                    loadFragment(BookmarksFragment.newInstance())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
