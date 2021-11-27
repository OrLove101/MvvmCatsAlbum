package com.orlove101.android.mvvmnewsapp.data.api

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.utils.QUERY_PAGE_SIZE
import retrofit2.HttpException

class EverythingNewsPageSource(
    private val newsApi: NewsAPI,
    private val query: String
): PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        if(query.isEmpty()) return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)

        val page: Int = params.key ?: 1
        val response = newsApi.searchForNews(pageNumber = page, query = query)

        if (response.isSuccessful) {
            val articles = checkNotNull(response.body()).articles.toList()
            val nextKey = if (articles.size < QUERY_PAGE_SIZE) null else page + 1;
            val prevKey = if (page == 1) null else page - 1;

            return LoadResult.Page(articles, prevKey, nextKey)
        }
        return LoadResult.Error(HttpException(response))
    }
}