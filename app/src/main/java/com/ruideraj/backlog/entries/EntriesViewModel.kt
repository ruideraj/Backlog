package com.ruideraj.backlog.entries

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Status
import com.ruideraj.backlog.data.EntriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesViewModel @Inject constructor(private val entriesRepository: EntriesRepository) : ViewModel() {

    companion object {
        private const val TAG = "EntriesViewModel"
    }

    sealed class Event {
        data class GoToEntryCreate(val listId: Long, val type: MediaType) : Event()
    }

    private var listId: Long = -1L

    private val _entries = MutableLiveData<List<Entry>>()
    val entries: LiveData<List<Entry>> = _entries

    private val _showCreateMenu = MutableLiveData(false)
    val showCreateMenu: LiveData<Boolean> = _showCreateMenu

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    fun loadEntries(listId: Long) {
        this.listId = listId
        if (listId < 0) throw IllegalArgumentException("Must include valid list id")

        viewModelScope.launch { entriesRepository.loadEntriesForList(listId).collect {
            _entries.value = it
        } }
    }

    fun onClickEntry(position: Int) {
        Log.d(TAG, "onClickEntry: $position")
    }

    fun onClickEntryStatus(position: Int) {
        _entries.value?.let {
            val entry = it[position]
            viewModelScope.launch {
                // TODO Should use a menu to select status
                val nextStatus = getNextStatus(entry.status)

                entriesRepository.setStatusForEntry(entry.id, nextStatus)
            }
        }
    }

    fun onClickCreateButton() {
        _showCreateMenu.value = !(_showCreateMenu.value ?: false)
    }

    fun onClickCreateMenuButton(type: MediaType) {
        _showCreateMenu.value = false
        viewModelScope.launch { eventChannel.send(Event.GoToEntryCreate(listId, type)) }
    }

    fun onBackPressed() {
        if (_showCreateMenu.value == true) {
            _showCreateMenu.value = false
        }
    }

    private fun getNextStatus(status: Status): Status {
        return when (status) {
            Status.TODO -> Status.IN_PROGRESS
            Status.IN_PROGRESS -> Status.DONE
            Status.DONE -> Status.TODO
        }
    }
}