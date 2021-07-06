package com.example.newsapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.NewsApplication
import com.example.newsapp.data.local.model.Article
import com.example.newsapp.data.local.model.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: NewsApplication, val newsRepository: NewsRepository) :
    AndroidViewModel(app) {

    val topHeadlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headLinesNewsPage = 1
    private var headlineNewsResponse: NewsResponse? = null

    val searchTopHeadlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    private var searchNewsResponse: NewsResponse? = null

    init {
        getTopHeadlines("id")
    }

    fun saveNews(article: Article) = viewModelScope.launch {
        newsRepository.updateNews(article)
    }

    fun deleteNews(article: Article) = viewModelScope.launch {
        newsRepository.deleteNews(article)
    }

    fun getAllNews() = newsRepository.getAllNews()

    fun getTopHeadlines(country: String) = viewModelScope.launch {
//        safeTopHeadlinesCall(country)
        topHeadlines.postValue(Resource.Loading())
        val response = newsRepository.getTopHeadlines(country, searchNewsPage)
        topHeadlines.postValue(handleSearchTopHeadLines(response))
    }


    fun getSearchTopHeadlines(query: String) = viewModelScope.launch {
//        safeSearchTopHeadlinesCall(query)
        searchTopHeadlines.postValue(Resource.Loading())
        val response = newsRepository.getSearchTopHeadlines(query, searchNewsPage)
        searchTopHeadlines.postValue(handleSearchTopHeadLines(response))
    }

    private fun handleSearchTopHeadLines(response: Response<NewsResponse>): Resource<NewsResponse>? {
        if (response.isSuccessful) {
            response.body()?.let {
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = it
                } else {
                    val oldArticle = searchNewsResponse?.articles
                    val newArticle = it.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(searchNewsResponse ?: it)
            }
        }
        return Resource.Error(response.message())
    }


    private fun handleTopHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                headLinesNewsPage++
                if (headlineNewsResponse == null) {
                    headlineNewsResponse = it
                } else {
                    val oldArticle = headlineNewsResponse?.articles
                    val newArticle = it.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(headlineNewsResponse ?: it)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeSearchTopHeadlinesCall(query: String) {
        searchTopHeadlines.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getSearchTopHeadlines(query, searchNewsPage)
                searchTopHeadlines.postValue(handleSearchTopHeadLines(response))
            } else {
                searchTopHeadlines.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchTopHeadlines.postValue(Resource.Error("Network Failure"))
                else -> searchTopHeadlines.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeTopHeadlinesCall(country: String) {
        topHeadlines.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getTopHeadlines(country, headLinesNewsPage)
                topHeadlines.postValue(handleTopHeadlinesResponse(response))
            } else {
                topHeadlines.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> topHeadlines.postValue(Resource.Error("Network Failure"))
                else -> topHeadlines.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }

        return false
    }
}
