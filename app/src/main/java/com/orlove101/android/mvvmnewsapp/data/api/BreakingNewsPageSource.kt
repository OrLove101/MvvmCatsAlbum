package com.orlove101.android.mvvmnewsapp.data.api

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.data.models.NewsResponse
import com.orlove101.android.mvvmnewsapp.util.QUERY_PAGE_SIZE
import retrofit2.HttpException

private const val TAG = "BreakingNewsPageSource"

class BreakingNewsPageSource(
    private val newsApi: NewsAPI
): PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page: Int = params.key ?: 1
        val response = newsApi.getBreakingNews(pageNumber = page)

        if (response.isSuccessful) {
            val articles = checkNotNull(response.body()).articles.toList()
            val nextKey = if (articles.size < QUERY_PAGE_SIZE) null else page + 1;
            val prevKey = if (page == 1) null else page - 1;

            return LoadResult.Page(articles, prevKey, nextKey)
        }
        return LoadResult.Error(HttpException(response))
    }
}