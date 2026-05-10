package com.example.fastconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.adapters.EventAdapter
import com.example.fastconnect.adapters.SocietyAdapter
import com.example.fastconnect.firebase.FirebaseHelper
import com.example.fastconnect.models.Event
import com.example.fastconnect.models.Society
import com.google.firebase.database.ValueEventListener

/**
 * SocietiesFragment — Displays societies and their events from Firebase (F2).
 *
 * Updated for Assignment#04: Uses Firebase Realtime Database with
 * real-time sync via ValueEventListener for both societies and announcements.
 * Changes made on one device are immediately reflected on other connected devices.
 */
class SocietiesFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var societyAdapter: SocietyAdapter
    private lateinit var rvSocieties: RecyclerView
    private lateinit var rvSocietiesList: RecyclerView

    private var societiesListener: ValueEventListener? = null
    private var announcementsListener: ValueEventListener? = null

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

        // Setup Societies List (Top horizontal)
        rvSocietiesList = view.findViewById(R.id.rvSocietiesList)
        rvSocietiesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Initialize with empty adapter
        societyAdapter = SocietyAdapter(emptyList()) { society ->
            // F2: Follow society via Firebase
            FirebaseHelper.followSociety(society.id) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Followed ${society.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvSocietiesList.adapter = societyAdapter

        // Setup Events RecyclerView (Bottom vertical)
        rvSocieties = view.findViewById(R.id.rvSocieties)
        rvSocieties.layoutManager = LinearLayoutManager(requireContext())

        eventAdapter = EventAdapter(emptyList(), { event ->
            Toast.makeText(
                requireContext(),
                "${event.title}\n${event.description}",
                Toast.LENGTH_LONG
            ).show()
        }, { eventToSave ->
            FirebaseHelper.saveEvent(eventToSave.id.toString()) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Saved Event!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        rvSocieties.adapter = eventAdapter

        // F2: Real-time sync for societies
        loadSocietiesFromFirebase()

        // F2: Real-time sync for society events (announcements of type "event")
        loadEventsFromFirebase()

        // Search/filter for society events
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
     * F2: Observe societies from Firebase with real-time listener.
     */
    private fun loadSocietiesFromFirebase() {
        societiesListener = FirebaseHelper.observeSocieties { societyList ->
            if (!isAdded) return@observeSocieties

            societyAdapter = SocietyAdapter(societyList) { society ->
                FirebaseHelper.followSociety(society.id) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Followed ${society.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            rvSocietiesList.adapter = societyAdapter
        }
    }

    /**
     * F2: Observe announcements of type "event" from Firebase with real-time listener.
     */
    private fun loadEventsFromFirebase() {
        announcementsListener = FirebaseHelper.observeAnnouncements("event") { announcementMaps ->
            if (!isAdded) return@observeAnnouncements

            val events = announcementMaps.mapIndexed { index, map ->
                val socIdStr = map["societyId"]?.toString()
                Event(
                    id = index + 1,
                    title = map["title"]?.toString() ?: "",
                    society = if (socIdStr != null) "Society Event" else "Course Event",
                    venue = "Main Campus",
                    date = map["date"]?.toString() ?: "",
                    category = map["category"]?.toString() ?: "",
                    description = map["description"]?.toString() ?: ""
                )
            }

            eventAdapter = EventAdapter(events, { event ->
                Toast.makeText(
                    requireContext(),
                    "${event.title}\n${event.description}",
                    Toast.LENGTH_LONG
                ).show()
            }, { eventToSave ->
                FirebaseHelper.saveEvent(eventToSave.id.toString()) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Saved Event!", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            rvSocieties.adapter = eventAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up Firebase listeners to prevent memory leaks
        societiesListener?.let { FirebaseHelper.removeSocietiesListener(it) }
        announcementsListener?.let { FirebaseHelper.removeAnnouncementsListener(it) }
    }
}
