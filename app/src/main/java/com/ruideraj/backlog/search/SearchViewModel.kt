package com.ruideraj.backlog.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.data.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository): ViewModel() {

    companion object {
        private const val TAG = "SearchViewModel"
        private const val PAGE_SIZE = 20
    }

    private var lastSearchInput = ""
    private var searchJob: Job? = null

    fun onSearchInputChanged(type: MediaType, input: String?) {
        if (!input.isNullOrBlank()) {
            if (lastSearchInput == input) {
                Log.d(TAG, "Same search input, skipping search")
                return
            }

            searchJob?.run { if (!isCompleted) {
                cancel()
            } }

            searchJob = viewModelScope.launch {
                lastSearchInput = input
                delay(2000)
                Log.d(TAG, "Run search with: $input")
                val results = searchRepository.searchByTitle(type, input, PAGE_SIZE)
                Log.d(TAG, results.toString())
            }
        } else {
            Log.d(TAG, "Blank search input")
            lastSearchInput = ""
        }
    }

}