package com.example.fastconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.models.NewsArticle

/**
 * RecyclerView Adapter for displaying news articles fetched from the REST API (F1).
 * Uses custom ViewHolder pattern.
 * Supports click and long-click callbacks for sharing and saving to bookmarks.
 */
class NewsAdapter(
    private var articles: List<NewsArticle>,
    private val onItemClick: (NewsArticle) -> Unit,
    private val onItemLongClick: (NewsArticle) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    /**
     * ViewHolder for News items.
     * Holds references to all views in item_news.xml.
     */
    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNewsTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        val tvNewsDescription: TextView = itemView.findViewById(R.id.tvNewsDescription)
        val tvNewsSource: TextView = itemView.findViewById(R.id.tvNewsSource)
        val tvNewsDate: TextView = itemView.findViewById(R.id.tvNewsDate)
        val btnShare: View = itemView.findViewById(R.id.btnShareNews)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(articles[position])
                }
            }
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(articles[position])
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.tvNewsTitle.text = article.title ?: "No Title"
        holder.tvNewsDescription.text = article.description ?: "No description available."
        holder.tvNewsSource.text = article.source?.name ?: "Unknown Source"
        holder.tvNewsDate.text = formatDate(article.publishedAt)

        holder.btnShare.setOnClickListener {
            onItemClick(articles[position])
        }
    }

    override fun getItemCount(): Int = articles.size

    /**
     * Updates the adapter data and refreshes the RecyclerView.
     */
    fun updateArticles(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    /**
     * Formats ISO date string to a more readable format.
     */
    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr.take(16).replace("T", " • ")
        }
    }
}
