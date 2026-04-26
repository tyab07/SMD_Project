package com.example.fastconnect.activities

import android.content.ContentValues
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
import com.example.fastconnect.db.FastConnectDbHelper

class AddAnnouncementActivity : AppCompatActivity() {

    private var societyIds = mutableListOf<Long>()

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

        rgCategory.check(R.id.rbCourse) // default
        rgType.check(R.id.rbAnnouncement) // default

        // Load societies for spinner
        val dbHelper = FastConnectDbHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${FastConnectDbHelper.TABLE_SOCIETIES}", null)
        val societyNames = mutableListOf<String>()
        
        cursor.use {
            while (it.moveToNext()) {
                societyIds.add(it.getLong(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_SOCIETY_ID)))
                societyNames.add(it.getString(it.getColumnIndexOrThrow(FastConnectDbHelper.COL_SOCIETY_NAME)))
            }
        }
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, societyNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSociety.adapter = adapter

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
            
            var societyId: Long? = null
            if (category == "society") {
                if (societyNames.isEmpty()) {
                    Toast.makeText(this, "Create a society first!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val selectedPos = spinnerSociety.selectedItemPosition
                societyId = societyIds[selectedPos]
            }

            val writeDb = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(FastConnectDbHelper.COL_ANN_TITLE, title)
                put(FastConnectDbHelper.COL_ANN_DESC, desc)
                put(FastConnectDbHelper.COL_ANN_CATEGORY, category)
                put(FastConnectDbHelper.COL_ANN_TYPE, type)
                put(FastConnectDbHelper.COL_ANN_DATE, date)
                if (societyId != null) {
                    put(FastConnectDbHelper.COL_ANN_SOCIETY_ID, societyId)
                }
            }
            
            writeDb.insert(FastConnectDbHelper.TABLE_ANNOUNCEMENTS, null, values)
            Toast.makeText(this, "Successfully created!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
