package com.example.newsapp.repository

import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.data.local.model.Article
import com.example.newsapp.data.remote.ApiClient

class NewsRepository(val db: NewsDatabase) {

    suspend fun getTopHeadlines(country: String, page: Int) =
        ApiClient.instance.getTopHeadlines(country, page)

    suspend fun getSearchTopHeadlines(query: String, page: Int) =
        ApiClient.instance.searchTopHeadlines(query, page)

    suspend fun updateNews(article: Article) =
        db.newsDao().updateNews(article)

    suspend fun deleteNews(article: Article) =
        db.newsDao().deleteNews(article)

    fun getAllNews() = db.newsDao().getAllNews()

}