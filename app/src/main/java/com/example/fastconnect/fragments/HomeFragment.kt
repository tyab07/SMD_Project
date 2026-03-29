package com.example.fastconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.adapters.EventAdapter
import com.example.fastconnect.models.Event

/**
 * HomeFragment - Displays welcome message and campus events.
 *
 * Requirement F1: Receives user data via Bundle (passed from DashboardActivity).
 * Requirement F3: Uses RecyclerView with EventAdapter + ViewHolder.
 * Requirement F5: Search/filter for events.
 */
class HomeFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var rvEvents: RecyclerView

    companion object {
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_EMAIL = "user_email"

        /**
         * Factory method to create HomeFragment with user data via Bundle.
         * Requirement F1 & F2: Data passing via Bundle.
         */
        fun newInstance(userName: String, userEmail: String): HomeFragment {
            val fragment = HomeFragment()
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // F1: Retrieve user data passed via Bundle
        val userName = arguments?.getString(ARG_USER_NAME) ?: "Student"
        val userEmail = arguments?.getString(ARG_USER_EMAIL) ?: ""

        // Set welcome text with user name from Bundle data
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcomeUser)
        tvWelcome.text = "Welcome, $userName! 👋"

        val tvSubtitle = view.findViewById<TextView>(R.id.tvWelcomeSubtitle)
        tvSubtitle.text = if (userEmail.isNotEmpty()) userEmail else "Stay connected with FAST"

        // F3: Setup RecyclerView with EventAdapter and ViewHolder
        rvEvents = view.findViewById(R.id.rvEvents)
        rvEvents.layoutManager = LinearLayoutManager(requireContext())

        val events = getSampleEvents()
        eventAdapter = EventAdapter(events) { event ->
            // Handle event click - could navigate to detail
            android.widget.Toast.makeText(
                requireContext(),
                "Event: ${event.title}\n${event.description}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        rvEvents.adapter = eventAdapter

        // F5: Setup search/filter for events
        val searchView = view.findViewById<SearchView>(R.id.searchViewEvents)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                eventAdapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                eventAdapter.filter.filter(newText)
                return true
            }
        })
    }

    /**
     * Sample event data for demonstration purposes.
     */
    private fun getSampleEvents(): List<Event> {
        return listOf(
            Event(
                id = 1,
                title = "AI Workshop: Introduction to Machine Learning",
                society = "FAST Computing Society",
                venue = "Lab 04, CS Building",
                date = "April 5, 2026",
                category = "Workshop",
                description = "Hands-on workshop covering ML basics with Python and TensorFlow."
            ),
            Event(
                id = 2,
                title = "Robotics Competition 2026",
                society = "Robotics Club",
                venue = "Main Auditorium",
                date = "April 12, 2026",
                category = "Competition",
                description = "Annual inter-university robotics competition. Register by April 8."
            ),
            Event(
                id = 3,
                title = "Annual Debate Championship",
                society = "Debating Society",
                venue = "Conference Hall B",
                date = "April 15, 2026",
                category = "Competition",
                description = "Parliamentary debate format. Open to all departments."
            ),
            Event(
                id = 4,
                title = "Photography Exhibition",
                society = "Media Club",
                venue = "Art Gallery, Block C",
                date = "April 18, 2026",
                category = "Exhibition",
                description = "Student photography showcase. Submit entries by April 10."
            ),
            Event(
                id = 5,
                title = "Career Fair 2026",
                society = "Career Development Cell",
                venue = "Sports Complex",
                date = "April 22, 2026",
                category = "Career",
                description = "Meet recruiters from top tech companies. Bring your CV!"
            ),
            Event(
                id = 6,
                title = "Hackathon: Build for Impact",
                society = "FAST Computing Society",
                venue = "Lab 01-03, CS Building",
                date = "April 25, 2026",
                category = "Hackathon",
                description = "24-hour hackathon focused on social impact projects."
            ),
            Event(
                id = 7,
                title = "Film Screening Night",
                society = "Media Club",
                venue = "Auditorium A",
                date = "April 28, 2026",
                category = "Entertainment",
                description = "Student-produced short films screening followed by Q&A."
            )
        )
    }
}
