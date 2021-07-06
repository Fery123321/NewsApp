package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.NewsApplication
import com.example.newsapp.R
import com.example.newsapp.data.adapter.NewsAdapter
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.databinding.FragmentBreakingNewsBinding
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.ui.NewsViewModelProviderFactory
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource
import com.google.android.material.snackbar.Snackbar

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentBreakingNewsBinding? = null
    private val binding get() = _binding!!
    private val TAG = "TopHeadlines"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBreakingNewsBinding.bind(view)
        setupRecyclerView()

        val newsRepository = NewsRepository(NewsDatabase(requireActivity()))
        val viewModelProviderFactory =
            NewsViewModelProviderFactory(NewsApplication(), newsRepository)
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelProviderFactory
        )[NewsViewModel::class.java]

        newsAdapter.setOnItemClickCallback {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }

        viewModel.topHeadlines.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    setProgressBar(false)
                    it.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.headLinesNewsPage == totalPages

                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    setProgressBar(false)
                    it.message?.let {
                        Snackbar.make(view, "Error occured: $it", Snackbar.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> setProgressBar(true)
            }
        })
    }

    private fun setProgressBar(state: Boolean) {
        binding.apply {
            if (state) {
                paginationProgressBar.visibility = View.VISIBLE
                isLoading = true
            } else {
                paginationProgressBar.visibility = View.GONE
                isLoading = false
            }
        }
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollingListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isLastItem && isNotBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getTopHeadlines("id")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.apply {
            rvBreakingNews.apply {
                adapter = newsAdapter
                layoutManager = LinearLayoutManager(activity)
                addOnScrollListener(this@BreakingNewsFragment.scrollingListener)
            }
        }
    }
}