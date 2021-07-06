package com.example.newsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.NewsApplication
import com.example.newsapp.R
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.databinding.ActivityNewsBinding
import com.example.newsapp.repository.NewsRepository

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel
    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = NewsRepository(NewsDatabase.invoke(this))
        val factory = NewsViewModelProviderFactory(application as NewsApplication, repository)
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]

        binding.bottomNavigationView.setupWithNavController(findNavController(R.id.newsNavHostFragment))
    }
}