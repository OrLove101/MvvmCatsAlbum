package com.orlove101.android.mvvmnewsapp.data.repository

import com.orlove101.android.mvvmnewsapp.data.api.BreakingNewsPageSource
import com.orlove101.android.mvvmnewsapp.data.api.EverythingNewsPageSource
import com.orlove101.android.mvvmnewsapp.data.api.NewsAPI
import com.orlove101.android.mvvmnewsapp.data.db.ArticleDatabase
import com.orlove101.android.mvvmnewsapp.data.models.Article
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    val db: ArticleDatabase,
    val api: NewsAPI
) {

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getSavedNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

    fun createBreakingNewsPageSource() = BreakingNewsPageSource(newsApi = api)

    fun createEverythingNewsPageSource(query: String) = EverythingNewsPageSource(
        newsApi = api,
        query = query
    )
}
