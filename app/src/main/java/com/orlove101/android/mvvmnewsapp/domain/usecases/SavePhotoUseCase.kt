package com.orlove101.android.mvvmnewsapp.domain.usecases

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.bumptech.glide.Glide
import com.orlove101.android.mvvmnewsapp.R
import com.orlove101.android.mvvmnewsapp.domain.models.SavePhotoParam
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

class SavePhotoUseCase @Inject constructor() {

    suspend operator fun invoke(
        savePhotoParam: SavePhotoParam
    ) {
        saveImage(
            image = Glide.with(savePhotoParam.view)
                .asBitmap()
                .load(savePhotoParam.imageUrl)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .submit()
                .get(),
            newsEventsChannel = savePhotoParam.newsEventsChannel
        )
    }

    private suspend fun saveImage(
        image: Bitmap,
        newsEventsChannel: Channel<NewsViewModel.NewsEvent>
    ): String? {
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

            newsEventsChannel.send(NewsViewModel.NewsEvent.ShowToastMessage(R.string.image_saved))
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
}