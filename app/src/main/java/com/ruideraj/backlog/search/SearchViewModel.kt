package com.ruideraj.backlog.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) : ViewModel() {

    sealed class Event {
        data class ReturnToEdit(val searchResult: SearchResult) : Event()
        data class ShowFilmDetails(val searchResult: SearchResult) : Event()
    }

    companion object {
        private const val INPUT_CHANGE_TIMEOUT = 1500L
    }

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