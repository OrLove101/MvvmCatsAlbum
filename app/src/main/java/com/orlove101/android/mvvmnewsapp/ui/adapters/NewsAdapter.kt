package com.orlove101.android.mvvmnewsapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.databinding.ItemArticlePreviewBinding

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemArticlePreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Article) {
            binding.apply {
                Glide.with(root).load(item.urlToImage).into(ivArticleImage)
                tvSource.text = item.source?.name
                tvTitle.text = item.title
                tvDescription.text = item.description
                tvPublishedAt.text = item.publishedAt
                tvTitle.setOnClickListener {
                    onItemClickListener?.let {
                        it(item)
                    }
                }
                tvDescription.setOnClickListener {
                    onItemClickListener?.let {
                        it(item)
                    }
                }
                ivArticleImage.setOnClickListener {
                    onImageClickListener?.let {
                        it(ivArticleImage, item.urlToImage.toString())
                    }
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsAdapter.ArticleViewHolder {
        val binding = ItemArticlePreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsAdapter.ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Article) -> Unit)? = null
    private var onImageClickListener: ((View, String) -> Unit)? = null

    fun setOnImageClickListener(listener: (View, String) -> Unit) {
        onImageClickListener = listener
    }

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }
}
