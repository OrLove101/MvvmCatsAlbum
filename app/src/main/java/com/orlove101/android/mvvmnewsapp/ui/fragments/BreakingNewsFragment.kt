package com.orlove101.android.mvvmnewsapp.ui.fragments

import android.Manifest
import android.animation.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orlove101.android.mvvmnewsapp.R
import com.orlove101.android.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsAdapter
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsLoaderStateAdapter
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import com.orlove101.android.mvvmnewsapp.util.AutoClearedValue
import com.orlove101.android.mvvmnewsapp.util.QUERY_PAGE_SIZE
import com.orlove101.android.mvvmnewsapp.util.Resource
import com.orlove101.android.mvvmnewsapp.util.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

@AndroidEntryPoint
class BreakingNewsFragment: Fragment() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Permission granted!")
            }
        }
    private var binding by autoCleared<FragmentBreakingNewsBinding>()
    private val viewModel: NewsViewModel by viewModels()
    private val newsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        NewsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        requestPermissions()

        lifecycleScope.launchWhenStarted {
            viewModel.breakingNews.collectLatest(newsAdapter::submitData)
        }

        return binding.root
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun saveImage(image: Bitmap): String? {
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

            activity?.runOnUiThread {
                Toast.makeText(activity, "Image Saved!", Toast.LENGTH_SHORT).show()
            }
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

    private fun setupRecyclerView() {
        binding.rvBreakingNews.apply {
            adapter = newsAdapter.withLoadStateHeaderAndFooter(
                header = NewsLoaderStateAdapter(),
                footer = NewsLoaderStateAdapter()
            )
            layoutManager = LinearLayoutManager(activity)
        }
        newsAdapter.addLoadStateListener { state: CombinedLoadStates ->
            binding.apply {
                rvBreakingNews.isVisible = state.refresh != LoadState.Loading
                paginationProgressBar.isVisible = state.refresh == LoadState.Loading
            }
        }
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
        newsAdapter.setOnImageClickListener { imageView, imageUrl ->
            //TODO SAVE ON LONG CLICK
            binding.fbSavePhoto.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    saveImage(Glide.with(binding.root)
                        .asBitmap()
                        .load(imageUrl)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .submit()
                        .get())
                }
            }
        }
    }
}

private const val TAG = "BreakingNewsFragment"
