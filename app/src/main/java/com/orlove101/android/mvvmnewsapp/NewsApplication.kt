package com.orlove101.android.mvvmnewsapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication @Inject constructor() : Application()