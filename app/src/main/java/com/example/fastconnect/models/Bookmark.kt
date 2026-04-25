package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a user bookmark stored in SQLite.
 * Linked to BookmarkFolder via folderId (Foreign Key relationship - F2).
 */
data class Bookmark(
    val id: Long = 0,
    val title: String,
    val url: String,
    val note: String = "",
    val folderId: Long,
    val folderName: String = "",
    val createdAt: String = ""
) : Serializable {
    companion object {
        private const val serialVersionUID = 5L
    }
}
