package com.example.fastconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fastconnect.R
import com.example.fastconnect.db.FastConnectDbHelper
import com.example.fastconnect.models.Bookmark
import com.example.fastconnect.models.BookmarkFolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AddEditBookmarkFragment - Form for creating and updating bookmarks (F3).
 *
 * F3 - Create: Inserts a new bookmark into the SQLite database.
 * F3 - Update: Updates an existing bookmark's fields in the SQLite database.
 * Threading: All database operations run on Dispatchers.IO via Kotlin Coroutines.
 * Enhanced: Folder management dialog for creating new folders.
 */
class AddEditBookmarkFragment : Fragment() {

    private lateinit var dbHelper: FastConnectDbHelper
    private lateinit var etTitle: TextInputEditText
    private lateinit var etUrl: TextInputEditText
    private lateinit var etNote: TextInputEditText
    private lateinit var spinnerFolder: Spinner
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnCreateFolder: MaterialButton
    private lateinit var tvHeader: TextView

    private var folders: List<BookmarkFolder> = emptyList()
    private var editingBookmark: Bookmark? = null

    companion object {
        private const val ARG_BOOKMARK = "bookmark_object"

        /**
         * Factory: Create fragment for ADD mode (no bookmark argument).
         */
        fun newInstance(): AddEditBookmarkFragment = AddEditBookmarkFragment()

        /**
         * Factory: Create fragment for EDIT mode (bookmark passed via Bundle).
         */
        fun newInstance(bookmark: Bookmark): AddEditBookmarkFragment {
            val fragment = AddEditBookmarkFragment()
            val args = Bundle()
            args.putSerializable(ARG_BOOKMARK, bookmark)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = FastConnectDbHelper(requireContext())

        // Bind views
        tvHeader = view.findViewById(R.id.tvAddEditHeader)
        etTitle = view.findViewById(R.id.etBookmarkTitle)
        etUrl = view.findViewById(R.id.etBookmarkUrl)
        etNote = view.findViewById(R.id.etBookmarkNote)
        spinnerFolder = view.findViewById(R.id.spinnerBookmarkFolder)
        btnSave = view.findViewById(R.id.btnSaveBookmark)
        btnCancel = view.findViewById(R.id.btnCancelBookmark)
        btnCreateFolder = view.findViewById(R.id.btnCreateFolder)

        // Check if editing an existing bookmark
        @Suppress("DEPRECATION")
        editingBookmark = arguments?.getSerializable(ARG_BOOKMARK) as? Bookmark
        if (editingBookmark != null) {
            tvHeader.text = "✏️ Edit Bookmark"
            btnSave.text = "Update Bookmark"
            populateFields(editingBookmark!!)
        }

        // Load folders into spinner
        loadFolders()

        // Save button action
        btnSave.setOnClickListener { saveBookmark() }

        // Cancel button action
        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Enhanced: Create new folder dialog
        btnCreateFolder.setOnClickListener { showCreateFolderDialog() }
    }

    /**
     * Loads folders from SQLite into the spinner on a background thread.
     */
    private fun loadFolders() {
        viewLifecycleOwner.lifecycleScope.launch {
            folders = withContext(Dispatchers.IO) { dbHelper.getAllFolders() }
            val folderNames = folders.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, folderNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFolder.adapter = adapter

            // If editing, set the spinner to the bookmark's folder
            editingBookmark?.let { bookmark ->
                val folderIndex = folders.indexOfFirst { it.id == bookmark.folderId }
                if (folderIndex >= 0) spinnerFolder.setSelection(folderIndex)
            }
        }
    }

    /**
     * Populates form fields with existing bookmark data (for edit mode).
     */
    private fun populateFields(bookmark: Bookmark) {
        etTitle.setText(bookmark.title)
        etUrl.setText(bookmark.url)
        etNote.setText(bookmark.note)
    }

    /**
     * F3 - Create/Update: Validates input and saves bookmark to SQLite on a background thread.
     */
    private fun saveBookmark() {
        val title = etTitle.text?.toString()?.trim() ?: ""
        val url = etUrl.text?.toString()?.trim() ?: ""
        val note = etNote.text?.toString()?.trim() ?: ""

        // Validate input
        if (title.isEmpty()) {
            etTitle.error = "Please enter a title"
            return
        }
        if (url.isEmpty()) {
            etUrl.error = "Please enter a URL"
            return
        }
        if (folders.isEmpty()) {
            Toast.makeText(requireContext(), "No folders available. Create one first.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedFolder = folders[spinnerFolder.selectedItemPosition]

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (editingBookmark != null) {
                    // F3 - Update: Update existing bookmark
                    dbHelper.updateBookmark(
                        id = editingBookmark!!.id,
                        title = title,
                        url = url,
                        note = note,
                        folderId = selectedFolder.id
                    )
                } else {
                    // F3 - Create: Insert new bookmark
                    dbHelper.insertBookmark(
                        title = title,
                        url = url,
                        note = note,
                        folderId = selectedFolder.id
                    )
                }
            }

            val message = if (editingBookmark != null) "✅ Bookmark updated!" else "✅ Bookmark saved!"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * Enhanced: Shows a dialog to create a new bookmark folder.
     * Inserts the folder into SQLite and refreshes the spinner.
     */
    private fun showCreateFolderDialog() {
        val input = EditText(requireContext())
        input.hint = "Folder name"
        input.setPadding(48, 32, 48, 16)

        AlertDialog.Builder(requireContext())
            .setTitle("Create New Folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isEmpty()) {
                    Toast.makeText(requireContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        dbHelper.insertFolder(folderName)
                    }
                    if (result > 0) {
                        Toast.makeText(requireContext(), "📁 Folder created!", Toast.LENGTH_SHORT).show()
                        loadFolders()
                    } else {
                        Toast.makeText(requireContext(), "Folder already exists or error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
