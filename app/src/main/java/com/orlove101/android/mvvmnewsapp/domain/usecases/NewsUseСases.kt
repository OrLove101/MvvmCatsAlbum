package com.orlove101.android.mvvmnewsapp.domain.usecases

import javax.inject.Inject

data class NewsUseCases @Inject constructor(
    val saveArticleUseCase: SaveArticleUseCase,
    val deleteArticleUseCase: DeleteArticleUseCase,
    val savePhotoUseCase: SavePhotoUseCase,
    val newsSelectedUseCase: NewsSelectedUseCase,
    val saveQueryUseCase: SaveQueryUseCase
)