package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.data.EntriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesViewModel @Inject constructor(private val entriesRepository: EntriesRepository) : ViewModel() {

    companion object {
        private const val TAG = "EntriesViewModel"
    }

    private val _entries = MutableLiveData<List<Entry>>()
    val entries: LiveData<List<Entry>> = _entries

    fun loadEntries(listId: Long) {
        viewModelScope.launch { entriesRepository.loadEntriesForList(listId).collect {
            _entries.value = it
        } }
    }

}