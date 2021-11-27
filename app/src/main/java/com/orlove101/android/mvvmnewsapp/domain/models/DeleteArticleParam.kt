package com.orlove101.android.mvvmnewsapp.domain.models

import com.orlove101.android.mvvmnewsapp.data.api.SavedNewsPageSource
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import kotlinx.coroutines.channels.Channel

data class DeleteArticleParam(
    val article: Article,
    val newsEventsChannel: Channel<NewsViewModel.NewsEvent>,
    val currentSavedPagingSource: SavedNewsPageSource?
)