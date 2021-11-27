package com.orlove101.android.mvvmnewsapp.domain.usecases

import com.orlove101.android.mvvmnewsapp.domain.models.SaveArticleParam
import com.orlove101.android.mvvmnewsapp.data.repository.NewsRepository
import javax.inject.Inject

class SaveArticleUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(
        saveArticleParam: SaveArticleParam
    ) {
        newsRepository.upsert(saveArticleParam.article)
        saveArticleParam.currentSavedPagingSource?.invalidate()
    }
}