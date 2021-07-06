package com.example.newsapp.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.data.local.model.Article
import com.example.newsapp.databinding.ArticleItemBinding

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsHolder>() {

    inner class NewsHolder(val binding: ArticleItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(
            oldItem: Article,
            newItem: Article
        ): Boolean = oldItem.url == newItem.url

        override fun areContentsTheSame(
            oldItem: Article,
            newItem: Article
        ): Boolean = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun getItemCount(): Int = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsHolder {
        val binding = ArticleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsHolder, position: Int) {
        val news = differ.currentList[position]
        holder.binding.apply {
            Glide.with(root).load(news.urlToImage).into(ivArticleImage)
            tvSource.text = news.source?.name
            tvDescription.text = news.description
            tvTitle.text = news.title
            tvPublishedAt.text = news.publishedAt

            root.setOnClickListener {
                onClickListener?.let { it(news) }
            }
        }
    }

    private var onClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickCallback(listener: (Article) -> Unit) {
        onClickListener = listener
    }

}