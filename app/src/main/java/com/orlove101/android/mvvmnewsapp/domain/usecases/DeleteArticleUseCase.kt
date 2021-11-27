package com.orlove101.android.mvvmnewsapp.domain.usecases

import com.orlove101.android.mvvmnewsapp.R
import com.orlove101.android.mvvmnewsapp.domain.models.DeleteArticleParam
import com.orlove101.android.mvvmnewsapp.data.repository.NewsRepository
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import javax.inject.Inject

class DeleteArticleUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(
        deleteArticleParam: DeleteArticleParam
    ) {
        newsRepository.deleteArticle(deleteArticleParam.article)
        deleteArticleParam.currentSavedPagingSource?.invalidate()
        deleteArticleParam.newsEventsChannel.send(
            NewsViewModel.NewsEvent.ShowArticleDeletedSnackbar(
                R.string.delete_article_snackbar_msg,
                R.string.delete_article_snackbar_action,
                deleteArticleParam.article
            )
        )
    }
}