package com.example.fastconnect.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fastconnect.R
import com.example.fastconnect.activities.NotificationInboxActivity
import com.example.fastconnect.activities.RoleSelectionActivity
import com.example.fastconnect.firebase.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

/**
 * ProfileFragment — Displays user profile information from Firebase Auth (F1).
 *
 * Updated for Assignment#04: Reads data from FirebaseAuth.currentUser
 * and Firebase Realtime Database. Uses FirebaseAuth.signOut() for logout.
 */
class ProfileFragment : Fragment() {

    companion object {
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_EMAIL = "user_email"

        fun newInstance(userName: String, userEmail: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_USER_NAME, userName)
            args.putString(ARG_USER_EMAIL, userEmail)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Get user data from Bundle or Firebase Auth
        val userName = arguments?.getString(ARG_USER_NAME)
            ?: currentUser?.displayName
            ?: "Muhammad Tayyab"
        val userEmail = arguments?.getString(ARG_USER_EMAIL)
            ?: currentUser?.email
            ?: "tayyab@fast.edu.pk"

        // Display user data
        view.findViewById<TextView>(R.id.tvProfileName).text = userName
        view.findViewById<TextView>(R.id.tvProfileEmail).text = "📧 $userEmail"

        // Notifications (Jetpack Compose screen)
        view.findViewById<TextView>(R.id.tvNotifications).setOnClickListener {
            startActivity(Intent(requireContext(), NotificationInboxActivity::class.java))
        }

        // Logout with Firebase Auth signOut
        view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            // Sign out from Firebase Auth
            auth.signOut()

            // Clear local session
            val prefs = requireContext().getSharedPreferences("FastConnectPrefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
