package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a bookmark folder/category stored in Firebase Realtime Database.
 * Parent node — bookmarks reference this via folderId.
 * Stored at /users/{uid}/folders/{id}.
 */
data class BookmarkFolder(
    val id: String = "",
    val name: String = "",
    val createdAt: String = ""
) : Serializable {
    companion object {
        private const val serialVersionUID = 6L
    }
    // No-arg constructor required for Firebase deserialization
    constructor() : this("", "", "")
}
