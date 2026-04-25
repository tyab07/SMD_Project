package com.example.fastconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.models.Bookmark

/**
 * RecyclerView Adapter for displaying bookmarks from SQLite (F3).
 * Uses custom ViewHolder pattern.
 * Supports edit, delete, and share callbacks.
 */
class BookmarkAdapter(
    private var bookmarks: List<Bookmark>,
    private val onEditClick: (Bookmark) -> Unit,
    private val onDeleteClick: (Bookmark) -> Unit,
    private val onShareClick: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    /**
     * ViewHolder for Bookmark items.
     * Holds references to all views in item_bookmark.xml.
     */
    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookmarkTitle: TextView = itemView.findViewById(R.id.tvBookmarkTitle)
        val tvBookmarkUrl: TextView = itemView.findViewById(R.id.tvBookmarkUrl)
        val tvBookmarkNote: TextView = itemView.findViewById(R.id.tvBookmarkNote)
        val tvBookmarkFolder: TextView = itemView.findViewById(R.id.tvBookmarkFolder)
        val tvBookmarkDate: TextView = itemView.findViewById(R.id.tvBookmarkDate)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditBookmark)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteBookmark)
        val btnShare: ImageButton = itemView.findViewById(R.id.btnShareBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = bookmarks[position]
        holder.tvBookmarkTitle.text = bookmark.title
        holder.tvBookmarkUrl.text = bookmark.url
        holder.tvBookmarkFolder.text = "📁 ${bookmark.folderName}"
        holder.tvBookmarkDate.text = bookmark.createdAt

        // Show or hide the note
        if (bookmark.note.isNotEmpty()) {
            holder.tvBookmarkNote.text = "📝 ${bookmark.note}"
            holder.tvBookmarkNote.visibility = View.VISIBLE
        } else {
            holder.tvBookmarkNote.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener { onEditClick(bookmark) }
        holder.btnDelete.setOnClickListener { onDeleteClick(bookmark) }
        holder.btnShare.setOnClickListener { onShareClick(bookmark) }
    }

    override fun getItemCount(): Int = bookmarks.size

    /**
     * Updates the adapter data and refreshes the RecyclerView.
     */
    fun updateBookmarks(newBookmarks: List<Bookmark>) {
        bookmarks = newBookmarks
        notifyDataSetChanged()
    }

    /**
     * Returns the bookmark at a given position (used for swipe-to-delete).
     */
    fun getBookmarkAt(position: Int): Bookmark = bookmarks[position]
}
