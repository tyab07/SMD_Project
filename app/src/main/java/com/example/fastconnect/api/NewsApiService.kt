package com.example.fastconnect.api

import com.example.fastconnect.models.NewsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the NewsAPI.org REST API (Requirement F1).
 *
 * Fetches top headlines as JSON and maps them to NewsResponse/NewsArticle models.
 * All calls are suspend functions executed on Dispatchers.IO via Kotlin Coroutines.
 */
interface NewsApiService {

    /**
     * Fetches top headlines from NewsAPI.org.
     * @param country Country code (default: "us")
     * @param category News category (default: "technology" — relevant to FAST university)
     * @param apiKey API key for authentication
     * @return NewsResponse containing a list of NewsArticle objects
     */
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "technology",
        @Query("pageSize") pageSize: Int = 30,
        @Query("apiKey") apiKey: String = API_KEY
    ): NewsResponse

    /**
     * Searches news articles by keyword.
     * @param query Search keyword
     * @param apiKey API key for authentication
     * @return NewsResponse containing matching articles
     */
    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 30,
        @Query("apiKey") apiKey: String = API_KEY
    ): NewsResponse

    companion object {
        private const val BASE_URL = "https://newsapi.org/"
        // Free-tier API key for NewsAPI.org
        const val API_KEY = "23b9afc35e8043c388b517e63a48e73e"

        /**
         * Singleton Retrofit client instance.
         * Uses OkHttp with logging interceptor for debugging.
         */
        val instance: NewsApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}
