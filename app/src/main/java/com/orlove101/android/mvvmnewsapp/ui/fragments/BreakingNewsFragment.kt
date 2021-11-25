package com.orlove101.android.mvvmnewsapp.ui.fragments

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.orlove101.android.mvvmnewsapp.R
import com.orlove101.android.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsAdapter
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsLoaderStateAdapter
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import com.orlove101.android.mvvmnewsapp.util.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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

        newsEventHandler()

        return binding.root
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
            // TODO make from viewModels event
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
        newsAdapter.setOnImageClickListener { imageView, imageUrl ->
            imageView.setOnLongClickListener {
                viewModel.saveImage(binding.root, imageUrl)
                true
            }
        }
    }

    private fun newsEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.newsEvent.collect { event ->
                when (event) {
                    is NewsViewModel.NewsEvent.ShowMessage -> {
                        Toast.makeText(activity, event.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

private const val TAG = "BreakingNewsFragment"
