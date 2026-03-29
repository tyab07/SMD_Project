package com.example.fastconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.models.Course

/**
 * RecyclerView Adapter for displaying Course items.
 * Implements Filterable for search/filter functionality (Requirement F5).
 * Uses custom ViewHolder pattern (Requirement F3).
 */
class CourseAdapter(
    private val allCourses: List<Course>,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>(), Filterable {

    // Filtered list shown in the RecyclerView
    private var filteredCourses: List<Course> = allCourses.toList()

    /**
     * ViewHolder for Course items (Requirement F3).
     * Holds references to all views in item_course.xml to avoid repeated findViewById calls.
     */
    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCourseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        val tvInstructor: TextView = itemView.findViewById(R.id.tvInstructor)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvRoom: TextView = itemView.findViewById(R.id.tvRoom)
        val tvCreditHours: TextView = itemView.findViewById(R.id.tvCreditHours)

        init {
            // Set click listener on the entire item view
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCourseClick(filteredCourses[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = filteredCourses[position]
        holder.tvCourseCode.text = course.code
        holder.tvCourseName.text = course.name
        holder.tvInstructor.text = course.instructor
        holder.tvTime.text = "⏰ ${course.time}"
        holder.tvRoom.text = "📍 ${course.room}"
        holder.tvCreditHours.text = "${course.creditHours} Credit Hrs"
    }

    override fun getItemCount(): Int = filteredCourses.size

    /**
     * Filter implementation for search functionality (Requirement F5).
     * Searches by course name, code, and instructor.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val results = FilterResults()

                results.values = if (query.isEmpty()) {
                    allCourses.toList()
                } else {
                    allCourses.filter { course ->
                        course.name.lowercase().contains(query) ||
                        course.code.lowercase().contains(query) ||
                        course.instructor.lowercase().contains(query)
                    }
                }
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCourses = results?.values as? List<Course> ?: allCourses
                notifyDataSetChanged()
            }
        }
    }
}
