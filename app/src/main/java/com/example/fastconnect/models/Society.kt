package com.example.fastconnect.models

/**
 * Data model representing a campus society stored in Firebase Realtime Database.
 * id maps to Firebase push key under /societies/{id}.
 */
data class Society(
    val id: String = "",
    val name: String = "",
    val description: String = ""
) {
    // No-arg constructor required for Firebase deserialization
    constructor() : this("", "", "")
}
