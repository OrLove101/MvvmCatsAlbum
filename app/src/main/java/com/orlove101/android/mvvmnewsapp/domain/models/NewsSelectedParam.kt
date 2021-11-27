package com.orlove101.android.mvvmnewsapp.domain.models

import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import kotlinx.coroutines.channels.Channel

data class NewsSelectedParam(
    val newsEventsChannel: Channel<NewsViewModel.NewsEvent>,
    val article: Article
)
