package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a bookmark folder/category stored in SQLite.
 * Parent table - bookmarks reference this via Foreign Key (F2).
 */
data class BookmarkFolder(
    val id: Long = 0,
    val name: String,
    val createdAt: String = ""
) : Serializable {
    companion object {
        private const val serialVersionUID = 6L
    }
}
