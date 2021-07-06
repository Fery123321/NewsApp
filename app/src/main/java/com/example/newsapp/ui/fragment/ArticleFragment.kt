package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.newsapp.NewsApplication
import com.example.newsapp.R
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.databinding.FragmentArticleBinding
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.ui.NewsViewModelProviderFactory

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentArticleBinding
    private val args: ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentArticleBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val repository = NewsRepository(NewsDatabase.invoke(requireActivity()))
        val factory = NewsViewModelProviderFactory(NewsApplication(), repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[NewsViewModel::class.java]

        val article = args.article
        binding.apply {
            webView.apply {
                webViewClient = WebViewClient()
                article.url?.let { loadUrl(it) }
            }

            fab.setOnClickListener {
                viewModel.saveNews(article)
            }
        }
    }
}