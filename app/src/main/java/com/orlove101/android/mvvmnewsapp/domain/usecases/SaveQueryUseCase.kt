package com.orlove101.android.mvvmnewsapp.domain.usecases

import com.orlove101.android.mvvmnewsapp.domain.models.SaveQueryParams
import javax.inject.Inject

class SaveQueryUseCase @Inject constructor() {

    operator fun invoke(
        saveQueryParams: SaveQueryParams
    ) {
        saveQueryParams._query.tryEmit(saveQueryParams.query)
    }
}