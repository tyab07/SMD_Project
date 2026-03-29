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
import com.example.fastconnect.adapters.CourseAdapter
import com.example.fastconnect.models.Course

/**
 * CoursesFragment - Displays registered courses in a RecyclerView.
 *
 * Requirement F3: RecyclerView with custom Adapter and ViewHolder.
 * Requirement F5: SearchView for filtering courses by name, code, or instructor.
 * Requirement F2: On item click, navigates to CourseDetailFragment via Bundle.
 * Requirement F4: Fragment transaction to switch to CourseDetailFragment.
 */
class CoursesFragment : Fragment() {

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var rvCourses: RecyclerView

    companion object {
        fun newInstance(): CoursesFragment {
            return CoursesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // F3: Setup RecyclerView with CourseAdapter and custom ViewHolder
        rvCourses = view.findViewById(R.id.rvCourses)
        rvCourses.layoutManager = LinearLayoutManager(requireContext())

        val courses = getSampleCourses()
        courseAdapter = CourseAdapter(courses) { course ->
            // F2 + F4: Navigate to CourseDetailFragment, passing Course via Bundle
            val detailFragment = CourseDetailFragment.newInstance(course)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack("course_detail") // Allow back navigation
                .commit()
        }
        rvCourses.adapter = courseAdapter

        // F5: Setup search/filter for courses
        val searchView = view.findViewById<SearchView>(R.id.searchViewCourses)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                courseAdapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                courseAdapter.filter.filter(newText)
                return true
            }
        })
    }

    /**
     * Sample course data for demonstration purposes.
     */
    private fun getSampleCourses(): List<Course> {
        return listOf(
            Course(
                id = 1,
                code = "CS-301",
                name = "Artificial Intelligence",
                instructor = "Dr. Ahmed Khan",
                time = "10:00 AM - 11:30 AM",
                room = "Room 204, CS Block",
                creditHours = 3,
                department = "Computer Science"
            ),
            Course(
                id = 2,
                code = "CS-402",
                name = "Mobile Application Development",
                instructor = "Sir Tayyab",
                time = "11:30 AM - 1:00 PM",
                room = "Lab 02, CS Block",
                creditHours = 3,
                department = "Computer Science"
            ),
            Course(
                id = 3,
                code = "CS-310",
                name = "Database Systems",
                instructor = "Dr. Fatima Noor",
                time = "2:00 PM - 3:30 PM",
                room = "Room 301, CS Block",
                creditHours = 3,
                department = "Computer Science"
            ),
            Course(
                id = 4,
                code = "SE-201",
                name = "Software Engineering",
                instructor = "Dr. Hassan Ali",
                time = "9:00 AM - 10:30 AM",
                room = "Room 105, SE Block",
                creditHours = 3,
                department = "Software Engineering"
            ),
            Course(
                id = 5,
                code = "CS-205",
                name = "Data Structures & Algorithms",
                instructor = "Dr. Saeed Akhtar",
                time = "1:00 PM - 2:30 PM",
                room = "Room 201, CS Block",
                creditHours = 4,
                department = "Computer Science"
            ),
            Course(
                id = 6,
                code = "MT-101",
                name = "Calculus & Analytical Geometry",
                instructor = "Prof. Nadia Bashir",
                time = "3:30 PM - 5:00 PM",
                room = "Room 401, Math Block",
                creditHours = 3,
                department = "Mathematics"
            )
        )
    }
}
