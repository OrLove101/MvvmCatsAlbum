package com.orlove101.android.mvvmnewsapp.data.db

import androidx.room.*
import com.orlove101.android.mvvmnewsapp.data.models.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long

    @Query("SELECT * FROM articles LIMIT :pageSize OFFSET :getFrom")
    fun getArticles(getFrom: Int, pageSize: Int): Flow<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}