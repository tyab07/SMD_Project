package com.example.fastconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.adapters.EventAdapter
import com.example.fastconnect.firebase.FirebaseHelper
import com.example.fastconnect.models.Event
import com.google.firebase.database.ValueEventListener

/**
 * HomeFragment — Displays welcome message and campus events from Firebase (F2).
 *
 * Updated for Assignment#04: Loads events from Firebase Realtime Database
 * with real-time sync using ValueEventListener. Falls back to sample data
 * if no events exist in Firebase yet.
 */
class HomeFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var rvEvents: RecyclerView
    private var announcementsListener: ValueEventListener? = null

    companion object {
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_EMAIL = "user_email"

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

        val userName = arguments?.getString(ARG_USER_NAME) ?: "Student"
        val userEmail = arguments?.getString(ARG_USER_EMAIL) ?: ""

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcomeUser)
        tvWelcome.text = "Welcome, $userName! 👋"

        val tvSubtitle = view.findViewById<TextView>(R.id.tvWelcomeSubtitle)
        tvSubtitle.text = if (userEmail.isNotEmpty()) userEmail else "Stay connected with FAST"

        val btnProfile = view.findViewById<android.widget.ImageView>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            // Navigate to ProfileFragment when profile icon is clicked
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment.newInstance(userName, userEmail))
                .addToBackStack("profile")
                .commit()
        }

        // Setup RecyclerView
        rvEvents = view.findViewById(R.id.rvEvents)
        rvEvents.layoutManager = LinearLayoutManager(requireContext())

        // Initialize with empty adapter, will be replaced by Firebase data
        eventAdapter = EventAdapter(emptyList(), { event ->
            Toast.makeText(
                requireContext(),
                "Event: ${event.title}\n${event.description}",
                Toast.LENGTH_LONG
            ).show()
        }, { eventToSave ->
            // F2: Save event to Firebase
            FirebaseHelper.saveEvent(eventToSave.id.toString()) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Saved Event!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show()
                }
            }
        })
        rvEvents.adapter = eventAdapter

        // F2: Load events from Firebase Realtime Database with real-time sync
        loadEventsFromFirebase()

        // Search/filter for events
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
     * F2: Observe announcements from Firebase with real-time listener.
     * Converts announcement maps to Event objects for the EventAdapter.
     */
    private fun loadEventsFromFirebase() {
        announcementsListener = FirebaseHelper.observeAnnouncements { announcementMaps ->
            if (!isAdded) return@observeAnnouncements

            val events = if (announcementMaps.isEmpty()) {
                // Fallback to sample events if Firebase has no data yet
                getSampleEvents()
            } else {
                announcementMaps.mapIndexed { index, map ->
                    Event(
                        id = index + 1,
                        title = map["title"]?.toString() ?: "",
                        society = map["societyId"]?.toString() ?: "Campus",
                        venue = "Main Campus",
                        date = map["date"]?.toString() ?: "",
                        category = map["category"]?.toString() ?: "",
                        description = map["description"]?.toString() ?: ""
                    )
                }
            }

            // Update adapter with new data (real-time sync)
            eventAdapter = EventAdapter(events, { event ->
                Toast.makeText(
                    requireContext(),
                    "Event: ${event.title}\n${event.description}",
                    Toast.LENGTH_LONG
                ).show()
            }, { eventToSave ->
                FirebaseHelper.saveEvent(eventToSave.id.toString()) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Saved Event!", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            rvEvents.adapter = eventAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up Firebase listener to prevent memory leaks
        announcementsListener?.let {
            FirebaseHelper.removeAnnouncementsListener(it)
        }
    }

    /**
     * Fallback sample event data when Firebase has no announcements yet.
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
