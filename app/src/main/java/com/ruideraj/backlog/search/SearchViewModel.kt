package com.ruideraj.backlog.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.SearchRepository
import com.ruideraj.backlog.util.Strings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository,
                                          private val strings: Strings) : ViewModel() {

    sealed class Event {
        data class ReturnToEdit(val searchResult: SearchResult) : Event()
        data class ShowFilmDetails(val searchResult: SearchResult) : Event()
    }

    companion object {
        private const val INPUT_CHANGE_TIMEOUT = 1500L
    }

    private val _uiVisibility = MutableLiveData<SearchUiState>()
    val uiVisibility: LiveData<SearchUiState> = _uiVisibility

    val searchResultsFlow: StateFlow<PagingData<SearchResult>>

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val queriesFlow: MutableSharedFlow<Pair<MediaType, String>> = MutableSharedFlow()

    init {
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
    }

    fun onQueryInputChanged(type: MediaType, query: String) {
        viewModelScope.launch { queriesFlow.emit(Pair(type, query)) }
    }

    fun onLoadStateChanged(loadState: CombinedLoadStates, itemCount: Int) {
        val isEmpty = loadState.refresh is LoadState.NotLoading && itemCount == 0
        val isError = loadState.refresh is LoadState.Error

        val isListVisible = loadState.refresh is LoadState.NotLoading && itemCount > 0
        val isProgressVisible = loadState.refresh is LoadState.Loading
        val isMessageVisible = isError || isEmpty

        val message = when {
            isError -> strings.getString(R.string.search_error)
            isEmpty -> strings.getString(R.string.search_empty)
            else -> ""
        }

        _uiVisibility.value = SearchUiState(isListVisible, isProgressVisible, isMessageVisible, isError, message)
    }

    fun onClickSearchResult(searchResult: SearchResult) {
        viewModelScope.launch {
            if (searchResult.type == MediaType.FILM) {
                eventChannel.send(Event.ShowFilmDetails(searchResult))
            } else {
                eventChannel.send(Event.ReturnToEdit(searchResult))
            }
        }
    }

    fun onConfirmDetails(searchResult: SearchResult) {
        viewModelScope.launch {
            eventChannel.send(Event.ReturnToEdit(searchResult))
        }
    }
}

const val PAGE_SIZE = 20
const val MOVIES_PAGE_SIZE = 10

data class SearchUiState(
    val isListVisible: Boolean,
    val isProgressVisible: Boolean,
    val isMessageVisible: Boolean,
    val isRetryButtonVisible: Boolean,
    val loadMessage: String
)