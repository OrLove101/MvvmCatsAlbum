package com.orlove101.android.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orlove101.android.mvvmnewsapp.R
import com.orlove101.android.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.orlove101.android.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsAdapter
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsLoaderStateAdapter
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import com.orlove101.android.mvvmnewsapp.util.QUERY_PAGE_SIZE
import com.orlove101.android.mvvmnewsapp.util.Resource
import com.orlove101.android.mvvmnewsapp.util.SEARCH_NEWS_TIME_DELAY
import com.orlove101.android.mvvmnewsapp.util.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchNewsFragment: Fragment() {
    private var binding by autoCleared<FragmentSearchNewsBinding>()
    private val viewModel: NewsViewModel by viewModels()
    private val newsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        NewsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchNewsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        setupSearch()

        lifecycleScope.launchWhenStarted {
            viewModel.searchNews
                .collectLatest(newsAdapter::submitData)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.query.onEach(::updateSearchQuery)
        }

        return binding.root
    }

    private fun setupSearch() {
        var job: Job? = null

        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty()) {
                        viewModel.setQuery(editable.toString())
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSearchNews.apply {
            adapter = newsAdapter
                .withLoadStateHeaderAndFooter(
                header = NewsLoaderStateAdapter(),
                footer = NewsLoaderStateAdapter()
                )
            layoutManager = LinearLayoutManager(activity)
        }
        newsAdapter.addLoadStateListener { state: CombinedLoadStates ->
            binding.apply {
                rvSearchNews.isVisible = state.refresh != LoadState.Loading
                paginationProgressBar.isVisible = state.refresh == LoadState.Loading
            }
        }
        newsAdapter.setOnItemClickListener {
            // TODO remove to viewModels event
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }

            // TODO make separate class to navigation
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

    private fun updateSearchQuery(searchQuery: String) {
        with(binding.etSearch) {
            if ((text?.toString() ?: "") != searchQuery) {
                setText(searchQuery)
            }
        }
    }
}
private const val TAG = "SearchNewsFragment"
