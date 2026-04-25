package com.example.fastconnect.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.adapters.BookmarkAdapter
import com.example.fastconnect.db.FastConnectDbHelper
import com.example.fastconnect.models.Bookmark
import com.example.fastconnect.models.BookmarkFolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * BookmarksFragment - Manages user bookmarks using SQLite CRUD (F3).
 *
 * F3: Full CRUD operations on bookmarks from local SQLite database.
 * F5: Dynamic SQL queries — search (LIKE), sort (ORDER BY), filter by folder.
 * Threading: All database operations run on Dispatchers.IO via Kotlin Coroutines.
 * Enhanced: Swipe-to-delete with undo, confirmation dialogs, share, empty state.
 */
class BookmarksFragment : Fragment() {

    private lateinit var dbHelper: FastConnectDbHelper
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var rvBookmarks: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerSort: Spinner
    private lateinit var spinnerFolder: Spinner
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var emptyLayout: View

    private var folders: List<BookmarkFolder> = emptyList()
    private var selectedFolderId: Long = -1L  // -1 = All folders
    private var currentSortIndex = 0

    companion object {
        fun newInstance(): BookmarksFragment = BookmarksFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = FastConnectDbHelper(requireContext())

        // Bind views
        rvBookmarks = view.findViewById(R.id.rvBookmarks)
        searchView = view.findViewById(R.id.searchViewBookmarks)
        spinnerSort = view.findViewById(R.id.spinnerSort)
        spinnerFolder = view.findViewById(R.id.spinnerFolder)
        fabAdd = view.findViewById(R.id.fabAddBookmark)
        emptyLayout = view.findViewById(R.id.layoutBookmarksEmpty)

        // Setup RecyclerView
        rvBookmarks.layoutManager = LinearLayoutManager(requireContext())
        bookmarkAdapter = BookmarkAdapter(
            bookmarks = emptyList(),
            onEditClick = { bookmark -> navigateToEdit(bookmark) },
            onDeleteClick = { bookmark -> confirmDelete(bookmark) },
            onShareClick = { bookmark -> shareBookmark(bookmark) }
        )
        rvBookmarks.adapter = bookmarkAdapter

        // Setup swipe-to-delete with undo (Enhanced)
        setupSwipeToDelete()

        // Setup sort spinner (F5 - ORDER BY)
        setupSortSpinner()

        // Setup folder filter spinner (F5 - WHERE clause)
        setupFolderSpinner()

        // Setup search (F5 - LIKE query)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchBookmarks(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchBookmarks(newText ?: "")
                return true
            }
        })

        // FAB → navigate to AddEditBookmarkFragment
        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddEditBookmarkFragment.newInstance())
                .addToBackStack("add_bookmark")
                .commit()
        }

        // Load initial data
        loadBookmarks()
    }

    override fun onResume() {
        super.onResume()
        // Reload when returning from add/edit
        loadFolders()
        loadBookmarks()
    }

    // ==================== F3: CRUD - Read ====================

    /**
     * F3 - Read: Loads all bookmarks from SQLite on a background thread.
     */
    private fun loadBookmarks() {
        viewLifecycleOwner.lifecycleScope.launch {
            val bookmarks = withContext(Dispatchers.IO) {
                if (selectedFolderId == -1L) {
                    when (currentSortIndex) {
                        1 -> dbHelper.getBookmarksSorted(FastConnectDbHelper.COL_BOOKMARK_TITLE, true)
                        2 -> dbHelper.getBookmarksSorted(FastConnectDbHelper.COL_BOOKMARK_TITLE, false)
                        3 -> dbHelper.getBookmarksSorted(FastConnectDbHelper.COL_BOOKMARK_CREATED_AT, true)
                        else -> dbHelper.getAllBookmarks()
                    }
                } else {
                    dbHelper.getBookmarksByFolder(selectedFolderId)
                }
            }
            bookmarkAdapter.updateBookmarks(bookmarks)
            updateEmptyState(bookmarks.isEmpty())
        }
    }

    // ==================== F5: Dynamic SQL - Search (LIKE) ====================

    /**
     * F5 - Search: Searches bookmarks using SQL LIKE operator.
     */
    private fun searchBookmarks(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val bookmarks = withContext(Dispatchers.IO) {
                if (query.isEmpty()) {
                    dbHelper.getAllBookmarks()
                } else {
                    dbHelper.searchBookmarks(query)
                }
            }
            bookmarkAdapter.updateBookmarks(bookmarks)
            updateEmptyState(bookmarks.isEmpty())
        }
    }

    // ==================== F5: Dynamic SQL - Sort (ORDER BY) ====================

    /**
     * F5: Setup sort spinner for ORDER BY queries.
     */
    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Newest First", "Title A–Z", "Title Z–A", "Oldest First")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = adapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                currentSortIndex = position
                loadBookmarks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ==================== F5: Dynamic SQL - Filter (WHERE) ====================

    /**
     * F5: Setup folder filter spinner.
     */
    private fun setupFolderSpinner() {
        loadFolders()
    }

    private fun loadFolders() {
        viewLifecycleOwner.lifecycleScope.launch {
            folders = withContext(Dispatchers.IO) { dbHelper.getAllFolders() }
            val folderNames = mutableListOf("All Folders")
            folderNames.addAll(folders.map { it.name })

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, folderNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFolder.adapter = adapter

            spinnerFolder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                    selectedFolderId = if (position == 0) -1L else folders[position - 1].id
                    loadBookmarks()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // ==================== F3: CRUD - Delete ====================

    /**
     * Enhanced: Confirmation dialog before deleting a bookmark.
     */
    private fun confirmDelete(bookmark: Bookmark) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Bookmark")
            .setMessage("Are you sure you want to delete \"${bookmark.title}\"?")
            .setPositiveButton("Delete") { _, _ -> deleteBookmark(bookmark) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * F3 - Delete: Removes a bookmark from SQLite on a background thread.
     */
    private fun deleteBookmark(bookmark: Bookmark) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deleteBookmark(bookmark.id)
            }
            loadBookmarks()
            Toast.makeText(requireContext(), "🗑️ Bookmark deleted", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== Enhanced: Swipe-to-Delete with Undo ====================

    /**
     * Enhanced: Swipe-to-delete with Snackbar undo action.
     */
    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bookmark = bookmarkAdapter.getBookmarkAt(position)

                // Delete the bookmark
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        dbHelper.deleteBookmark(bookmark.id)
                    }
                    loadBookmarks()

                    // Show Snackbar with undo option
                    Snackbar.make(rvBookmarks, "Bookmark deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            viewLifecycleOwner.lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    dbHelper.insertBookmark(
                                        bookmark.title, bookmark.url,
                                        bookmark.note, bookmark.folderId
                                    )
                                }
                                loadBookmarks()
                            }
                        }
                        .show()
                }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(rvBookmarks)
    }

    // ==================== Navigation ====================

    /**
     * F3 - Update: Navigate to AddEditBookmarkFragment in edit mode.
     */
    private fun navigateToEdit(bookmark: Bookmark) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddEditBookmarkFragment.newInstance(bookmark))
            .addToBackStack("edit_bookmark")
            .commit()
    }

    /**
     * Enhanced: Share bookmark via Android share Intent.
     */
    private fun shareBookmark(bookmark: Bookmark) {
        val shareText = "${bookmark.title}\n${bookmark.url}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, bookmark.title)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    // ==================== UI Helpers ====================

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvBookmarks.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
