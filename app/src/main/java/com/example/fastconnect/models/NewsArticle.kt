package com.example.fastconnect.models

import java.io.Serializable

/**
 * Data model representing a news article from the REST API.
 * Maps to the JSON response from NewsAPI.org.
 */
data class NewsArticle(
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val source: NewsSource?
) : Serializable {
    companion object {
        private const val serialVersionUID = 3L
    }
}

data class NewsSource(
    val id: String?,
    val name: String?
) : Serializable {
    companion object {
        private const val serialVersionUID = 4L
    }
}
