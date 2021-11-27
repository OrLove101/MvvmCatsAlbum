package com.orlove101.android.mvvmnewsapp.ui.viewModels

import android.view.View
import androidx.lifecycle.*
import androidx.paging.*
import com.orlove101.android.mvvmnewsapp.data.api.SavedNewsPageSource
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.data.repository.NewsRepository
import com.orlove101.android.mvvmnewsapp.domain.models.*
import com.orlove101.android.mvvmnewsapp.domain.usecases.NewsUseCases
import com.orlove101.android.mvvmnewsapp.utils.QUERY_PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NewsViewModel"

// TODO separate models by layers
// TODO make dark/light theme and change in settings
// TODO tests

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val newsUseCases: NewsUseCases
) : ViewModel() {
    val breakingNews: StateFlow<PagingData<Article>> = Pager<Int, Article>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )
    ) {
        // TODO usecase
        newsRepository.createBreakingNewsPageSource()
        // TODO -----
    }.flow
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    var currentSavedPagingSource: SavedNewsPageSource? = null

    val savedNews: StateFlow<PagingData<Article>> = Pager<Int, Article>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )
    ) {
        // TODO usecase or all pager creation logic
        newsRepository.createSavedNewsPageSource().also { currentSavedPagingSource = it }
        // TODO -----
    }.flow
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchNews: StateFlow<PagingData<Article>> = query
        .map {
            newSearchPager(it)
        }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private val newsEventsChannel = Channel<NewsEvent>()
    val newsEvent = newsEventsChannel.receiveAsFlow()

    fun saveArticle(article: Article) = viewModelScope.launch {
        val params = SaveArticleParam(
            article = article,
            currentSavedPagingSource = currentSavedPagingSource
        )
        newsUseCases.saveArticleUseCase(saveArticleParam = params)
    }

    fun deleteArticle(article: Article) = viewModelScope.launch {
        val params = DeleteArticleParam(
            article = article,
            currentSavedPagingSource = currentSavedPagingSource,
            newsEventsChannel = newsEventsChannel
        )
        newsUseCases.deleteArticleUseCase(deleteArticleParam = params)
    }

    fun saveImage(view: View, imageUrl: String) = CoroutineScope(Dispatchers.IO).launch {
        val param = SavePhotoParam(
            view = view,
            imageUrl = imageUrl,
            newsEventsChannel = newsEventsChannel
        )
        newsUseCases.savePhotoUseCase(savePhotoParam = param)
    }

    fun onNewsSelected(article: Article) {
        viewModelScope.launch {
            val param = NewsSelectedParam(
                newsEventsChannel = newsEventsChannel,
                article = article
            )
            newsUseCases.newsSelectedUseCase(param)
        }
    }

    private fun newSearchPager(query: String): Pager<Int, Article> {
        return Pager(
            PagingConfig(
                pageSize = QUERY_PAGE_SIZE,
                initialLoadSize = QUERY_PAGE_SIZE,
                prefetchDistance = 1,
                enablePlaceholders = true
            )) {
                newsRepository.createEverythingNewsPageSource(query = query)
            }
    }

    fun setQuery(query: String) {
        val param = SaveQueryParams(_query = _query, query = query)

        newsUseCases.saveQueryUseCase(saveQueryParams = param)
    }

    sealed class NewsEvent {
        data class ShowToastMessage(val msgId: Int): NewsEvent()
        data class NavigateToArticleScreen(val article: Article): NewsEvent()
        data class ShowArticleDeletedSnackbar(
            val msgId: Int,
            val actonMsgId: Int,
            val article: Article
        ): NewsEvent()
    }
}
