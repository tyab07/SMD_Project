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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.adapters.BookmarkAdapter
import com.example.fastconnect.firebase.FirebaseHelper
import com.example.fastconnect.models.Bookmark
import com.example.fastconnect.models.BookmarkFolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * BookmarksFragment — Manages user bookmarks using Firebase Realtime Database (F2).
 *
 * Updated for Assignment#04: All CRUD operations now use FirebaseHelper
 * instead of local SQLite. Data is stored per-user at /users/{uid}/bookmarks/.
 * Search, sort, and folder filter are done client-side after fetching from Firebase.
 */
class BookmarksFragment : Fragment() {

    private lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var rvBookmarks: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerSort: Spinner
    private lateinit var spinnerFolder: Spinner
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var emptyLayout: View

    private var folders: List<BookmarkFolder> = emptyList()
    private var selectedFolderId: String = ""  // empty = All folders
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

        // Setup swipe-to-delete with undo
        setupSwipeToDelete()

        // Setup sort spinner
        setupSortSpinner()

        // Setup folder filter spinner
        setupFolderSpinner()

        // Setup search
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
        loadFolders()
        loadBookmarks()
    }

    // ==================== Read (Firebase) ====================

    /**
     * Loads all bookmarks from Firebase Realtime Database.
     */
    private fun loadBookmarks() {
        if (selectedFolderId.isEmpty()) {
            when (currentSortIndex) {
                1 -> FirebaseHelper.getBookmarksSorted("title", true) { updateUI(it) }
                2 -> FirebaseHelper.getBookmarksSorted("title", false) { updateUI(it) }
                3 -> FirebaseHelper.getBookmarksSorted("createdAt", true) { updateUI(it) }
                else -> FirebaseHelper.getAllBookmarks { updateUI(it) }
            }
        } else {
            FirebaseHelper.getBookmarksByFolder(selectedFolderId) { updateUI(it) }
        }
    }

    private fun updateUI(bookmarks: List<Bookmark>) {
        if (!isAdded) return
        bookmarkAdapter.updateBookmarks(bookmarks)
        updateEmptyState(bookmarks.isEmpty())
    }

    // ==================== Search (Firebase) ====================

    private fun searchBookmarks(query: String) {
        FirebaseHelper.searchBookmarks(query) { updateUI(it) }
    }

    // ==================== Sort ====================

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

    // ==================== Folder Filter ====================

    private fun setupFolderSpinner() {
        loadFolders()
    }

    private fun loadFolders() {
        FirebaseHelper.getAllFolders { folderList ->
            if (!isAdded) return@getAllFolders
            folders = folderList
            val folderNames = mutableListOf("All Folders")
            folderNames.addAll(folders.map { it.name })

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, folderNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFolder.adapter = adapter

            spinnerFolder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                    selectedFolderId = if (position == 0) "" else folders[position - 1].id
                    loadBookmarks()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // ==================== Delete (Firebase) ====================

    private fun confirmDelete(bookmark: Bookmark) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Bookmark")
            .setMessage("Are you sure you want to delete \"${bookmark.title}\"?")
            .setPositiveButton("Delete") { _, _ -> deleteBookmark(bookmark) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        FirebaseHelper.deleteBookmark(bookmark.id) { success ->
            if (!isAdded) return@deleteBookmark
            if (success) {
                loadBookmarks()
                Toast.makeText(requireContext(), "🗑️ Bookmark deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== Swipe-to-Delete ====================

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bookmark = bookmarkAdapter.getBookmarkAt(position)

                FirebaseHelper.deleteBookmark(bookmark.id) { success ->
                    if (!isAdded) return@deleteBookmark
                    if (success) {
                        loadBookmarks()
                        Snackbar.make(rvBookmarks, "Bookmark deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                FirebaseHelper.insertBookmark(
                                    bookmark.title, bookmark.url,
                                    bookmark.note, bookmark.folderId
                                ) { undoSuccess ->
                                    if (undoSuccess) loadBookmarks()
                                }
                            }
                            .show()
                    }
                }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(rvBookmarks)
    }

    // ==================== Navigation ====================

    private fun navigateToEdit(bookmark: Bookmark) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddEditBookmarkFragment.newInstance(bookmark))
            .addToBackStack("edit_bookmark")
            .commit()
    }

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
