package com.orlove101.android.mvvmnewsapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.databinding.ItemArticlePreviewBinding

class NewsAdapter: PagingDataAdapter<Article, NewsAdapter.ArticleViewHolder>(ArticleDifferCallback) {

    inner class ArticleViewHolder(private val binding: ItemArticlePreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Article?) {
            item?.let {
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
    }

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
        holder.bind(getItem(position))
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

private object ArticleDifferCallback: DiffUtil.ItemCallback<Article>() {

    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}
