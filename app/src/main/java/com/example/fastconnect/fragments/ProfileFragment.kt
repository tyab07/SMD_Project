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
import com.example.fastconnect.activities.RoleSelectionActivity

/**
 * ProfileFragment - Displays user profile information.
 *
 * Requirement F1: Receives user data (name, email) via Bundle,
 *   originally passed from SignInActivity → DashboardActivity → ProfileFragment.
 */
class ProfileFragment : Fragment() {

    companion object {
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_EMAIL = "user_email"

        /**
         * Factory method to create ProfileFragment with user data via Bundle.
         * Requirement F1: Data passing via Bundle from Activity to Fragment.
         */
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

        // F1: Retrieve user data from Bundle
        val userName = arguments?.getString(ARG_USER_NAME) ?: "Muhammad Tayyab"
        val userEmail = arguments?.getString(ARG_USER_EMAIL) ?: "tayyab@fast.edu.pk"

        // Display user data received via Bundle
        view.findViewById<TextView>(R.id.tvProfileName).text = userName
        view.findViewById<TextView>(R.id.tvProfileEmail).text = "📧 $userEmail"

        view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
