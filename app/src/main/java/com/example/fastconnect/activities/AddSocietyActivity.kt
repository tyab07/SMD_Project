package com.example.fastconnect.activities

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fastconnect.R
import com.example.fastconnect.db.FastConnectDbHelper

class AddSocietyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_society)

        val etName = findViewById<EditText>(R.id.etSocietyName)
        val etDesc = findViewById<EditText>(R.id.etSocietyDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitSociety)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FastConnectDbHelper(this).writableDatabase
            val values = ContentValues().apply {
                put(FastConnectDbHelper.COL_SOCIETY_NAME, name)
                put(FastConnectDbHelper.COL_SOCIETY_DESC, desc)
            }
            db.insert(FastConnectDbHelper.TABLE_SOCIETIES, null, values)
            
            Toast.makeText(this, "Society created successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
