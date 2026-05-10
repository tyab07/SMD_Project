package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a user bookmark stored in Firebase Realtime Database.
 * Linked to BookmarkFolder via folderId (logical relationship in Firebase).
 * Stored at /users/{uid}/bookmarks/{id}.
 */
data class Bookmark(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val note: String = "",
    val folderId: String = "",
    val folderName: String = "",
    val createdAt: String = ""
) : Serializable {
    companion object {
        private const val serialVersionUID = 5L
    }
    // No-arg constructor required for Firebase deserialization
    constructor() : this("", "", "", "", "", "", "")
}
