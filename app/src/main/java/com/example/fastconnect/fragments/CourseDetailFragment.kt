package com.example.fastconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.fastconnect.R
import com.example.fastconnect.models.Course

/**
 * CourseDetailFragment - Displays full details of a selected course.
 *
 * Requirement F2: Receives a Course object via Bundle (putSerializable / getSerializable).
 * Requirement F4: Fragment transaction — navigated here from CoursesFragment, with back stack support.
 */
class CourseDetailFragment : Fragment() {

    companion object {
        private const val ARG_COURSE = "course_object"

        /**
         * Factory method to create CourseDetailFragment with course data via Bundle.
         * Requirement F2: Transfer a custom object (Course) from RecyclerView to Detail Fragment.
         */
        fun newInstance(course: Course): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_COURSE, course) // F2: Bundle data transfer
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // F2: Retrieve Course object from Bundle
        @Suppress("DEPRECATION")
        val course = arguments?.getSerializable(ARG_COURSE) as? Course

        course?.let {
            // Populate all detail views with Bundle data
            view.findViewById<TextView>(R.id.tvDetailCode).text = it.code
            view.findViewById<TextView>(R.id.tvDetailName).text = it.name
            view.findViewById<TextView>(R.id.tvDetailInstructor).text = it.instructor
            view.findViewById<TextView>(R.id.tvDetailTime).text = "⏰ ${it.time}"
            view.findViewById<TextView>(R.id.tvDetailRoom).text = it.room
            view.findViewById<TextView>(R.id.tvDetailCredits).text = "${it.creditHours} Credits"
            view.findViewById<TextView>(R.id.tvDetailDepartment).text = "Department: ${it.department}"
        }

        // F4: Back navigation via fragment back stack
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
