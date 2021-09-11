package com.ruideraj.backlog.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) : ViewModel() {

    companion object {
        private const val INPUT_CHANGE_TIMEOUT = 1500L
    }

    val searchResultsFlow: StateFlow<PagingData<SearchResult>>

    val queryInput: (type: MediaType, query: String) -> Unit

    init {
        val queriesFlow = MutableSharedFlow<Pair<MediaType, String>>()

        searchResultsFlow = queriesFlow
            .debounce(INPUT_CHANGE_TIMEOUT) // Wait some time for user to stop typing before searching
            .filter { it.second.trim().isNotBlank() }
            .distinctUntilChangedBy { it.second }
            .flatMapLatest { (type, query) ->
                searchRepository.getTitleSearchStream(type, query).cachedIn(viewModelScope)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = PagingData.empty()
            )

        queryInput = { type, query ->
            viewModelScope.launch { queriesFlow.emit(Pair(type, query)) }
        }
    }
}

const val PAGE_SIZE = 20