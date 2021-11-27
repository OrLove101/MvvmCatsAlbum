package com.orlove101.android.mvvmnewsapp.domain.models

import kotlinx.coroutines.flow.MutableStateFlow

data class SaveQueryParams(
    val _query: MutableStateFlow<String>,
    val query: String
)