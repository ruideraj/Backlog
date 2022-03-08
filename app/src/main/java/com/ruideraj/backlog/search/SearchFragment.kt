package com.ruideraj.backlog.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.SearchResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        private const val TAG = "SearchFragment"
    }

    private val viewModel by viewModels<SearchViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()

        val type = args.getSerializable(Constants.ARG_TYPE) as MediaType

        val toolbar = view.findViewById<Toolbar>(R.id.search_toolbar).apply {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        toolbar.findViewById<SearchView>(R.id.search_input).apply {
            setIconifiedByDefault(false)
            maxWidth = Integer.MAX_VALUE
            val typePluralRes = when (type) {
                MediaType.FILM -> R.string.films
                MediaType.SHOW -> R.string.shows
                MediaType.GAME -> R.string.games
                MediaType.BOOK -> R.string.books
            }
            queryHint = getString(R.string.search_hint, getString(typePluralRes))

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.onQueryInputChanged(type, newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.onQueryInputChanged(type, query)
                    return true
                }
            })
        }

        val recycler = view.findViewById<RecyclerView>(R.id.search_recycler).apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        val adapter = SearchAdapter { searchResult ->
            viewModel.onClickSearchResult(searchResult)
        }
        recycler.adapter = adapter.withLoadStateHeaderAndFooter(
            header = SearchLoadStateAdapter { adapter.retry() },
            footer = SearchLoadStateAdapter { adapter.retry() }
        )
        val progressBar = view.findViewById<ProgressBar>(R.id.search_progress)
        val loadMessage = view.findViewById<TextView>(R.id.search_message)
        val retryButton = view.findViewById<Button>(R.id.search_retry).apply {
            setOnClickListener { adapter.retry() }
        }

        viewModel.let {
            it.uiVisibility.observe(viewLifecycleOwner) { uiState ->
                recycler.isVisible = uiState.isListVisible
                progressBar.isVisible = uiState.isProgressVisible
                loadMessage.isVisible = uiState.isMessageVisible
                retryButton.isVisible = uiState.isRetryButtonVisible
                loadMessage.text = uiState.loadMessage
            }

            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    it.searchResultsFlow.collectLatest { results ->
                        adapter.submitData(results)
                        recycler.scrollToPosition(0)
                    }
                }
            }

            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    adapter.loadStateFlow.collect { loadState ->
                        viewModel.onLoadStateChanged(loadState, adapter.itemCount)
                    }
                }
            }

            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    it.eventFlow.collect { event ->
                        when (event) {
                            is SearchViewModel.Event.ReturnToEdit -> {
                                findNavController().run {
                                    previousBackStackEntry?.savedStateHandle
                                        ?.set(Constants.ARG_SEARCH_RESULT, event.searchResult)
                                    navigateUp()
                                }
                            }
                            is SearchViewModel.Event.ShowFilmDetails -> {
                                val directions = SearchFragmentDirections
                                    .actionSearchFragmentToSearchDetailsDialog(event.searchResult)
                                findNavController().navigate(directions)
                            }
                        }
                    }
                }
            }
        }

        findNavController().apply {
            val selfBackStackEntry = getBackStackEntry(R.id.searchFragment)
            selfBackStackEntry.savedStateHandle.getLiveData<SearchResult>(Constants.ARG_SEARCH_RESULT)
                .observe(viewLifecycleOwner) { searchResult ->
                    selfBackStackEntry.savedStateHandle.remove<SearchResult>(Constants.ARG_SEARCH_RESULT)
                    viewModel.onConfirmDetails(searchResult)
                }
        }
    }
}