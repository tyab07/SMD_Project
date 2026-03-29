package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a campus event or announcement.
 * Implements Serializable to allow passing via Intent Extras and Bundles.
 */
data class Event(
    val id: Int,
    val title: String,
    val society: String,
    val venue: String,
    val date: String,
    val category: String,
    val description: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
