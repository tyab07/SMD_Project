package com.example.fastconnect.models

/**
 * Wrapper data class for the NewsAPI.org JSON response.
 * Contains status, total results count, and the list of articles.
 */
data class NewsResponse(
    val status: String?,
    val totalResults: Int?,
    val articles: List<NewsArticle>?
)
