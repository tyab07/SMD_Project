package com.example.fastconnect.models

/**
 * Data model representing an app user stored in Firebase Realtime Database.
 * uid maps to Firebase Auth UID.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user" // "admin" or "user"
) {
    // No-arg constructor required for Firebase deserialization
    constructor() : this("", "", "", "user")
}
