package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a university course.
 * Implements Serializable to allow passing via Intent Extras and Bundles.
 */
data class Course(
    val id: Int,
    val code: String,
    val name: String,
    val instructor: String,
    val time: String,
    val room: String,
    val creditHours: Int,
    val department: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
