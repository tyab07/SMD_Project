package com.example.fastconnect.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.example.fastconnect.firebase.FirebaseHelper
import com.example.fastconnect.models.Society

/**
 * AddAnnouncementActivity — Admin screen to create announcements/events.
 *
 * Updated for Assignment#04: Reads societies from Firebase for spinner,
 * writes announcements to Firebase Realtime Database at /announcements/{pushId}.
 */
class AddAnnouncementActivity : AppCompatActivity() {

    private var societies = mutableListOf<Society>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_announcement)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val rgCategory = findViewById<RadioGroup>(R.id.rgCategory)
        val rbCourse = findViewById<RadioButton>(R.id.rbCourse)
        val rbSocietyCat = findViewById<RadioButton>(R.id.rbSocietyCat)
        val spinnerSociety = findViewById<Spinner>(R.id.spinnerSociety)
        val rgType = findViewById<RadioGroup>(R.id.rgType)
        val rbAnnouncement = findViewById<RadioButton>(R.id.rbAnnouncement)
        val rbEvent = findViewById<RadioButton>(R.id.rbEvent)
        val etDate = findViewById<EditText>(R.id.etDate)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitAnn)

        rgCategory.check(R.id.rbCourse)
        rgType.check(R.id.rbAnnouncement)

        // F2: Load societies from Firebase for spinner
        FirebaseHelper.getAllSocieties { societyList ->
            societies.clear()
            societies.addAll(societyList)
            val societyNames = societies.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, societyNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSociety.adapter = adapter
        }

        rgCategory.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbSocietyCat) {
                spinnerSociety.visibility = View.VISIBLE
            } else {
                spinnerSociety.visibility = View.GONE
            }
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val date = etDate.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = if (rbCourse.isChecked) "course" else "society"
            val type = if (rbAnnouncement.isChecked) "announcement" else "event"

            var societyId: String? = null
            if (category == "society") {
                if (societies.isEmpty()) {
                    Toast.makeText(this, "Create a society first!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val selectedPos = spinnerSociety.selectedItemPosition
                societyId = societies[selectedPos].id
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating..."

            // F2: Write to Firebase Realtime Database
            FirebaseHelper.addAnnouncement(
                title = title,
                description = desc,
                category = category,
                type = type,
                date = date,
                societyId = societyId
            ) { success ->
                if (success) {
                    Toast.makeText(this, "Successfully created!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit"
                    Toast.makeText(this, "Failed to create. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
