package com.example.fastconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.models.Event
import com.google.android.material.chip.Chip

/**
 * RecyclerView Adapter for displaying Event items.
 * Implements Filterable for category-based search/filter functionality (Requirement F5).
 * Uses custom ViewHolder pattern (Requirement F3).
 */
class EventAdapter(
    private val allEvents: List<Event>,
    private val onEventClick: (Event) -> Unit,
    private val onSaveClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>(), Filterable {

    // Filtered list shown in the RecyclerView
    private var filteredEvents: List<Event> = allEvents.toList()

    /**
     * ViewHolder for Event items (Requirement F3).
     * Holds references to all views in item_event.xml.
     */
    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chipCategory: Chip = itemView.findViewById(R.id.chipCategory)
        val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvSociety: TextView = itemView.findViewById(R.id.tvSociety)
        val tvVenue: TextView = itemView.findViewById(R.id.tvVenue)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnSaveEvent: android.widget.ImageView = itemView.findViewById(R.id.btnSaveEvent)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEventClick(filteredEvents[position])
                }
            }
            btnSaveEvent.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSaveClick(filteredEvents[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = filteredEvents[position]
        holder.chipCategory.text = event.category
        holder.tvEventTitle.text = event.title
        holder.tvSociety.text = event.society
        holder.tvVenue.text = "📍 ${event.venue}"
        holder.tvDate.text = "📅 ${event.date}"
    }

    override fun getItemCount(): Int = filteredEvents.size

    /**
     * Filter implementation for search functionality (Requirement F5).
     * Searches by event title, society, category, and venue.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val results = FilterResults()

                results.values = if (query.isEmpty()) {
                    allEvents.toList()
                } else {
                    allEvents.filter { event ->
                        event.title.lowercase().contains(query) ||
                        event.society.lowercase().contains(query) ||
                        event.category.lowercase().contains(query) ||
                        event.venue.lowercase().contains(query)
                    }
                }
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredEvents = results?.values as? List<Event> ?: allEvents
                notifyDataSetChanged()
            }
        }
    }
}
