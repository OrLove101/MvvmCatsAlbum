package com.orlove101.android.mvvmnewsapp.ui.viewModels

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.*
import com.bumptech.glide.Glide
import com.orlove101.android.mvvmnewsapp.data.models.Article
import com.orlove101.android.mvvmnewsapp.data.models.NewsResponse
import com.orlove101.android.mvvmnewsapp.data.repository.NewsRepository
import com.orlove101.android.mvvmnewsapp.util.QUERY_PAGE_SIZE
import com.orlove101.android.mvvmnewsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

private const val TAG = "NewsViewModel"

// TODO make settings string
// TODO make dark/light theme and change in settings
// TODO cleanArchitecture (use cases etc)
// TODO tests

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {
    val breakingNews: StateFlow<PagingData<Article>> = Pager<Int, Article>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )
    ) {
        newsRepository.createBreakingNewsPageSource()
    }.flow
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    val savedNews: StateFlow<PagingData<Article>> = Pager<Int, Article>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )
    ) {
        newsRepository.createSavedNewsPageSource()
    }.flow
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchNews: StateFlow<PagingData<Article>> = query
        .map {
            newSearchPager(it)
        }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    //TODO ask for god naming
    private val newsEventsChannel = Channel<NewsEvent>()
    val newsEvent = newsEventsChannel.receiveAsFlow()

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun saveImage(view: View, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            saveImage(
                Glide.with(view)
                .asBitmap()
                .load(imageUrl)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .submit()
                .get()
            )
        }
    }

    private suspend fun saveImage(image: Bitmap): String? {
        var savedImagePath: String? = null
        val imageFileName = "JPEG_" + UUID.randomUUID() + ".jpg"
        val storageDir = File(
            Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM + "/"
        )
        var success = true

        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.getAbsolutePath()
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            galleryAddPic(savedImagePath)

            // TODO fix hardcoded string
            newsEventsChannel.send(NewsEvent.ShowMessage("Image Saved!"))
        }
        return savedImagePath
    }

    private fun galleryAddPic(imagePath: String?) {
        imagePath?.let { path ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(path)
            val contentUri: Uri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
        }
    }

    private fun newSearchPager(query: String): Pager<Int, Article> {
        return Pager(PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = true
        )) {
            newsRepository.createEverythingNewsPageSource(query = query)
        }
    }

    fun setQuery(query: String) {
        _query.tryEmit(query)
    }

    sealed class NewsEvent {
        data class ShowMessage(val msg: String): NewsEvent()
    }
}
