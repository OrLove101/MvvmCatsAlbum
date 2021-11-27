package com.orlove101.android.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.orlove101.android.mvvmnewsapp.databinding.FragmentSavedNewsBinding
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsAdapter
import com.orlove101.android.mvvmnewsapp.ui.adapters.NewsLoaderStateAdapter
import com.orlove101.android.mvvmnewsapp.ui.viewModels.NewsViewModel
import com.orlove101.android.mvvmnewsapp.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SavedNewsFragment: Fragment() {
    private var binding by autoCleared<FragmentSavedNewsBinding>()
    private val viewModel: NewsViewModel by viewModels()
    private val newsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        NewsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedNewsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        lifecycleScope.launchWhenCreated {
            viewModel.savedNews.collectLatest(newsAdapter::submitData)
        }

        newsEventHandler()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemTouchHelperCallback = getItemTouchHelper()

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }
    }

    private fun getItemTouchHelper(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.snapshot()[position]

                article?.let { viewModel.deleteArticle(it) }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSavedNews.apply {
            adapter = newsAdapter.withLoadStateHeaderAndFooter(
                header = NewsLoaderStateAdapter(),
                footer = NewsLoaderStateAdapter()
            )
            layoutManager = LinearLayoutManager(activity)
        }
        newsAdapter.setOnItemClickListener { article ->
            viewModel.onNewsSelected(article)
        }
    }

    private fun newsEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.newsEvent.collectLatest { event ->
                when(event) {
                    is NewsViewModel.NewsEvent.NavigateToArticleScreen -> {
                        val action = SavedNewsFragmentDirections
                            .actionSavedNewsFragmentToArticleFragment(event.article)

                        findNavController().navigate(action)
                    }
                    is NewsViewModel.NewsEvent.ShowArticleDeletedSnackbar -> {
                        Snackbar.make(binding.root, getString(event.msgId), Snackbar.LENGTH_LONG)
                            .apply {
                                setAction(getString(event.actonMsgId)) {
                                    viewModel.saveArticle(event.article)
                                }
                                show()
                            }
                    }
                }
            }
        }
    }
}