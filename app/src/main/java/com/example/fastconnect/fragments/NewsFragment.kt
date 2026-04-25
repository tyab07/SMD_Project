package com.example.fastconnect.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fastconnect.R
import com.example.fastconnect.adapters.NewsAdapter
import com.example.fastconnect.api.NewsApiService
import com.example.fastconnect.db.FastConnectDbHelper
import com.example.fastconnect.models.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NewsFragment - Displays top headlines from a REST API (Requirement F1).
 *
 * F1: Fetches JSON data from NewsAPI.org using Retrofit and displays in RecyclerView.
 * Threading: All network calls run on Dispatchers.IO via Kotlin Coroutines.
 * Enhanced: Pull-to-refresh, long-press to save as bookmark, share functionality.
 */
class NewsFragment : Fragment() {

    private lateinit var newsAdapter: NewsAdapter
    private lateinit var rvNews: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: View
    private lateinit var emptyLayout: View
    private lateinit var emptyMessage: TextView
    private lateinit var dbHelper: FastConnectDbHelper

    companion object {
        fun newInstance(): NewsFragment = NewsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = FastConnectDbHelper(requireContext())

        // Bind views
        rvNews = view.findViewById(R.id.rvNews)
        swipeRefresh = view.findViewById(R.id.swipeRefreshNews)
        progressBar = view.findViewById(R.id.progressNews)
        emptyLayout = view.findViewById(R.id.layoutNewsEmpty)
        emptyMessage = view.findViewById(R.id.tvNewsEmptyMessage)

        // Setup RecyclerView
        rvNews.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(
            articles = emptyList(),
            onItemClick = { article -> shareArticle(article) },
            onItemLongClick = { article -> saveArticleAsBookmark(article) }
        )
        rvNews.adapter = newsAdapter

        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener { fetchNews() }

        // Initial fetch
        fetchNews()
    }

    /**
     * F1: Fetches news from REST API using Kotlin Coroutines on Dispatchers.IO.
     * Updates UI on Dispatchers.Main after data is received.
     */
    private fun fetchNews() {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading()
            try {
                // Network call on background thread (Dispatchers.IO)
                val response = withContext(Dispatchers.IO) {
                    NewsApiService.instance.getTopHeadlines()
                }

                // Update UI on main thread
                val articles = response.articles?.filter {
                    !it.title.isNullOrEmpty() && it.title != "[Removed]"
                } ?: emptyList()

                if (articles.isNotEmpty()) {
                    newsAdapter.updateArticles(articles)
                    showContent()
                } else {
                    showEmpty("No news articles found.\nPull down to refresh.")
                }
            } catch (e: Exception) {
                showEmpty("Failed to load news.\n${e.localizedMessage}\n\nPull down to refresh.")
            }
        }
    }

    /**
     * Enhanced: Share a news article via Android share Intent.
     */
    private fun shareArticle(article: NewsArticle) {
        val shareText = "${article.title}\n\n${article.url ?: ""}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, article.title ?: "News Article")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    /**
     * Enhanced: Long-press to save a news article as a bookmark in SQLite.
     * Bridges the API and Database modules.
     */
    private fun saveArticleAsBookmark(article: NewsArticle) {
        AlertDialog.Builder(requireContext())
            .setTitle("Save to Bookmarks")
            .setMessage("Save \"${article.title}\" to your bookmarks?")
            .setPositiveButton("Save") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        // Get or create "Saved News" folder
                        val folders = dbHelper.getAllFolders()
                        val savedNewsFolder = folders.find { it.name == "Saved News" }
                        val folderId = savedNewsFolder?.id ?: dbHelper.insertFolder("Saved News")

                        dbHelper.insertBookmark(
                            title = article.title ?: "News Article",
                            url = article.url ?: "",
                            note = article.description ?: "",
                            folderId = folderId
                        )
                    }
                    Toast.makeText(requireContext(), "✅ Saved to Bookmarks!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ==================== UI State Helpers ====================

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rvNews.visibility = View.GONE
        emptyLayout.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        rvNews.visibility = View.VISIBLE
        emptyLayout.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showEmpty(message: String) {
        progressBar.visibility = View.GONE
        rvNews.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        emptyMessage.text = message
        swipeRefresh.isRefreshing = false
    }
}
