package com.example.fastconnect.models

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String // "admin" or "user"
)
