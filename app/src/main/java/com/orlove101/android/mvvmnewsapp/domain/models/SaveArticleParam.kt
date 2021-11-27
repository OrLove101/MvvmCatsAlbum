package com.orlove101.android.mvvmnewsapp.domain.models

import com.orlove101.android.mvvmnewsapp.data.api.SavedNewsPageSource
import com.orlove101.android.mvvmnewsapp.data.models.Article

class SaveArticleParam (
    val article: Article,
    val currentSavedPagingSource: SavedNewsPageSource?
)