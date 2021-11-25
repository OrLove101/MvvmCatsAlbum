package com.orlove101.android.mvvmnewsapp.ui.viewModels

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.data.models.NewsResponse
import com.orlove101.android.mvvmnewsapp.data.repository.NewsRepository
import com.orlove101.android.mvvmnewsapp.util.QUERY_PAGE_SIZE
import com.orlove101.android.mvvmnewsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "NewsViewModel"

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {
    val breakingNews: StateFlow<PagingData<Article>> = Pager<Int, Article>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )
    ) {
        newsRepository.createBreakingNewsPageSource()
    }.flow
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    //private var newPagingSource: PagingSource<*, *>? = null

    val searchNews: StateFlow<PagingData<Article>> = query
        .map {
            newSearchPager(it)
        }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    val savedNews = newsRepository.getSavedNews().asLiveData()

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private fun newSearchPager(query: String): Pager<Int, Article> {
        return Pager(PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )) {
            newsRepository.createEverythingNewsPageSource(query = query)
//                .also {
//                newPagingSource = it
//            }
        }
    }

    fun setQuery(query: String) {
        _query.tryEmit(query)
    }
}
