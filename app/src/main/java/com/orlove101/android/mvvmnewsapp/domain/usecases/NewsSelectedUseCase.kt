package com.orlove101.android.mvvmnewsapp.domain.usecases

import com.orlove101.android.mvvmnewsapp.domain.models.NewsSelectedParam
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import javax.inject.Inject

class NewsSelectedUseCase @Inject constructor() {

    suspend operator fun invoke(
        newsSelectedParam: NewsSelectedParam
    ) {
        newsSelectedParam.newsEventsChannel.send(
            NewsViewModel.NewsEvent.NavigateToArticleScreen(
                newsSelectedParam.article
            )
        )
    }
}