package com.example.fastconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.adapters.EventAdapter
import com.example.fastconnect.models.Event

/**
 * SocietiesFragment - Displays society events in a RecyclerView.
 *
 * Requirement F3: RecyclerView with custom Adapter and ViewHolder.
 * Requirement F5: Search/filter for society events.
 */
class SocietiesFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var rvSocieties: RecyclerView

    companion object {
        fun newInstance(): SocietiesFragment {
            return SocietiesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_societies_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // F3: Setup RecyclerView with EventAdapter and custom ViewHolder
        rvSocieties = view.findViewById(R.id.rvSocieties)
        rvSocieties.layoutManager = LinearLayoutManager(requireContext())

        val societyEvents = getSocietyEvents()
        eventAdapter = EventAdapter(societyEvents) { event ->
            android.widget.Toast.makeText(
                requireContext(),
                "${event.title}\n${event.description}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        rvSocieties.adapter = eventAdapter

        // F5: Setup search/filter for society events
        val searchView = view.findViewById<SearchView>(R.id.searchViewSocieties)
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
     * Sample society event data for demonstration purposes.
     */
    private fun getSocietyEvents(): List<Event> {
        return listOf(
            Event(
                id = 101,
                title = "Coding Bootcamp: Web Development",
                society = "FAST Computing Society",
                venue = "Lab 01, CS Building",
                date = "April 3, 2026",
                category = "Workshop",
                description = "3-day bootcamp covering HTML, CSS, JavaScript fundamentals."
            ),
            Event(
                id = 102,
                title = "Line Follower Robot Challenge",
                society = "Robotics Club",
                venue = "Engineering Lab",
                date = "April 7, 2026",
                category = "Competition",
                description = "Build and race line-following robots. Teams of 2-3 students."
            ),
            Event(
                id = 103,
                title = "Public Speaking Workshop",
                society = "Debating Society",
                venue = "Seminar Hall A",
                date = "April 10, 2026",
                category = "Workshop",
                description = "Improve your public speaking skills with professional trainers."
            ),
            Event(
                id = 104,
                title = "Campus Vlog Competition",
                society = "Media Club",
                venue = "Online Submission",
                date = "April 14, 2026",
                category = "Competition",
                description = "Create a 3-minute vlog about campus life. Top 3 win prizes!"
            ),
            Event(
                id = 105,
                title = "Open Source Contribution Day",
                society = "FAST Computing Society",
                venue = "Lab 03, CS Building",
                date = "April 20, 2026",
                category = "Hackathon",
                description = "Contribute to real open source projects with mentorship."
            ),
            Event(
                id = 106,
                title = "Drone Racing Championship",
                society = "Robotics Club",
                venue = "Sports Ground",
                date = "April 24, 2026",
                category = "Competition",
                description = "First-person-view drone racing event. Exciting prizes await!"
            )
        )
    }
}
