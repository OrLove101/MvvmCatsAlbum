package com.orlove101.android.mvvmnewsapp.domain.models

import android.view.View
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import kotlinx.coroutines.channels.Channel

data class SavePhotoParam(
    val view: View,
    val imageUrl: String,
    val newsEventsChannel: Channel<NewsViewModel.NewsEvent>,
)