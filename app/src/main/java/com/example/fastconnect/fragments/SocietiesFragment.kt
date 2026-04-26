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
import com.example.fastconnect.adapters.SocietyAdapter
import com.example.fastconnect.db.FastConnectDbHelper
import com.example.fastconnect.models.Event
import com.example.fastconnect.models.Society

/**
 * SocietiesFragment - Displays society events in a RecyclerView.
 *
 * Requirement F3: RecyclerView with custom Adapter and ViewHolder.
 * Requirement F5: Search/filter for society events.
 */
class SocietiesFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var societyAdapter: SocietyAdapter
    private lateinit var rvSocieties: RecyclerView
    private lateinit var rvSocietiesList: RecyclerView

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

        val dbHelper = FastConnectDbHelper(requireContext())
        val db = dbHelper.readableDatabase

        val prefs = requireContext().getSharedPreferences("FastConnectPrefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getLong("USER_ID", -1L)

        // F3: Setup Societies List (Top horizontal)
        rvSocietiesList = view.findViewById(R.id.rvSocietiesList)
        rvSocietiesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        val allSocieties = dbHelper.getAllSocieties()
        societyAdapter = SocietyAdapter(allSocieties) { society ->
            dbHelper.followSociety(userId, society.id)
            android.widget.Toast.makeText(requireContext(), "Followed ${society.name}", android.widget.Toast.LENGTH_SHORT).show()
        }
        rvSocietiesList.adapter = societyAdapter

        // F3: Setup Events RecyclerView (Bottom vertical)
        rvSocieties = view.findViewById(R.id.rvSocieties)
        rvSocieties.layoutManager = LinearLayoutManager(requireContext())

        val societyEvents = getSocietyEventsFromDb(dbHelper)
        eventAdapter = EventAdapter(societyEvents, { event ->
            android.widget.Toast.makeText(
                requireContext(),
                "${event.title}\n${event.description}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }, { eventToSave ->
            dbHelper.saveEvent(userId, eventToSave.id.toLong())
            android.widget.Toast.makeText(requireContext(), "Saved Event!", android.widget.Toast.LENGTH_SHORT).show()
        })
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
     * Load events from DB Annoucements table where type is event
     */
    private fun getSocietyEventsFromDb(dbHelper: FastConnectDbHelper): List<Event> {
        val events = mutableListOf<Event>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${FastConnectDbHelper.TABLE_ANNOUNCEMENTS} WHERE type = 'event'", null)
        
        cursor.use {
            while (it.moveToNext()) {
                val socIdIndex = it.getColumnIndex(FastConnectDbHelper.COL_ANN_SOCIETY_ID)
                val socIdStr = if (socIdIndex >= 0 && !it.isNull(socIdIndex)) {
                    "Society #${it.getLong(socIdIndex)}"
                } else {
                    "Course Event"
                }

                events.add(Event(
                    id = it.getLong(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_ANN_ID)).toInt(),
                    title = it.getString(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_ANN_TITLE)),
                    society = socIdStr,
                    venue = "Main Campus", // Add venue to DB later if needed
                    date = it.getString(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_ANN_DATE)),
                    category = it.getString(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_ANN_CATEGORY)),
                    description = it.getString(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_ANN_DESC))
                ))
            }
        }
        return events
    }
}
