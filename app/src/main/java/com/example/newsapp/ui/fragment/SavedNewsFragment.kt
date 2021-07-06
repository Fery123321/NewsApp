package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.NewsApplication
import com.example.newsapp.R
import com.example.newsapp.data.adapter.NewsAdapter
import com.example.newsapp.data.local.NewsDatabase
import com.example.newsapp.databinding.FragmentSavedNewsBinding
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.ui.NewsViewModelProviderFactory
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

    private lateinit var viewModel: NewsViewModel 
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentSavedNewsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSavedNewsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val repository = NewsRepository(NewsDatabase.invoke(requireActivity()))
        val factory = NewsViewModelProviderFactory(NewsApplication(), repository)
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]
        setupRecyclerView()

        newsAdapter.setOnItemClickCallback {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                viewModel.deleteNews(article)
                Snackbar.make(view, "Article Deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveNews(article)
                    }
                }.show()
            }
        }

        ItemTouchHelper(itemTouchHelper).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }

        viewModel.getAllNews().observe(viewLifecycleOwner, Observer {
            newsAdapter.differ.submitList(it)
        })
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.apply {
            rvSavedNews.apply {
                adapter = newsAdapter
                layoutManager = LinearLayoutManager(activity)
            }
        }
    }
}