package com.example.newsapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newsapp.data.local.model.Article

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNews(article: Article): Long

    @Query("SELECT * FROM article_table")
    fun getAllNews(): LiveData<List<Article>>

    @Delete
    suspend fun deleteNews(article: Article)
}