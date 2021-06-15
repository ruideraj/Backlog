package com.ruideraj.backlog.entries

import androidx.lifecycle.*
import com.ruideraj.backlog.BacklogList
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
        object NavigateUp : Event()
        data class GoToEntryCreate(val listId: Long, val type: MediaType) : Event()
        data class GoToEntryView(val listId: Long, val type: MediaType, val entry: Entry) : Event()
        data class EntrySelectedChanged(val position: Int) : Event()
        object SelectedEntriesCleared : Event()
        data class ShowDeleteConfirmation(val count: Int) : Event()
    }

    private lateinit var list: BacklogList

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _entries = MutableLiveData<List<Entry>>()
    val entries: LiveData<List<Entry>> = _entries

    private val _showCreateMenu = MutableLiveData(true)
    val showCreateMenu: LiveData<Boolean> = _showCreateMenu

    private val _expandCreateMenu = MutableLiveData(false)
    val expandCreateMenu: LiveData<Boolean> = _expandCreateMenu

    private val _selectMode = MutableLiveData(false)
    val selectMode: LiveData<Boolean> = _selectMode

    private val _selectedEntries = mutableSetOf<Entry>()
    val selectedEntries: Set<Entry> = _selectedEntries

    private val _backPressedCallbackEnabled = MediatorLiveData<Boolean>()
    val backPressedCallbackEnabled: LiveData<Boolean> = _backPressedCallbackEnabled

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    init {
        _backPressedCallbackEnabled.addSource(_expandCreateMenu) { determineBackPressedCallbackEnabled() }
        _backPressedCallbackEnabled.addSource(_selectMode) { determineBackPressedCallbackEnabled() }
    }

    fun loadEntries(backlogList: BacklogList) {
        list = backlogList
        _title.value = backlogList.title
        viewModelScope.launch {
            entriesRepository.loadEntriesForList(backlogList.id).collect {
                _entries.value = it
            }
        }
    }

    fun onScrollUp() {
        if (_selectMode.value != true) {
            _showCreateMenu.value = true
        }
    }

    fun onScrollDown() {
        if (_selectMode.value != true) {
            _showCreateMenu.value = false
        }
    }

    fun onClickEntry(position: Int) {
        _entries.value?.let {
            val entry = it[position]

            if (_selectMode.value == true) {
                if (_selectedEntries.contains(entry)) {
                    _selectedEntries.remove(entry)
                } else {
                    _selectedEntries.add(entry)
                }

                viewModelScope.launch { eventChannel.send(Event.EntrySelectedChanged(position)) }

                _title.value = _selectedEntries.size.toString()
            } else {
                viewModelScope.launch {
                    eventChannel.send(Event.GoToEntryView(list.id, entry.type, entry))
                }
            }
        }
    }

    fun onLongClickEntry(position: Int) {
        if (_selectMode.value == false) {
            setSelectMode(true)
            onClickEntry(position)
        }
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
        _expandCreateMenu.value = !(_expandCreateMenu.value ?: false)
    }

    fun onClickCreateMenuButton(type: MediaType) {
        _expandCreateMenu.value = false
        viewModelScope.launch { list.let { eventChannel.send(Event.GoToEntryCreate(it.id, type)) } }
    }

    fun onBackPressed() {
        if (_expandCreateMenu.value == true) {
            _expandCreateMenu.value = false
        } else if (_selectMode.value == true) {
            setSelectMode(false)
        }
    }

    fun onClickNavigationIcon() {
        if (_selectMode.value == true) {
            setSelectMode(false)
        } else {
            viewModelScope.launch { eventChannel.send(Event.NavigateUp) }
        }
    }

    fun onClickDelete() {
        if (_selectedEntries.size > 0) {
            viewModelScope.launch { eventChannel.send(Event.ShowDeleteConfirmation(_selectedEntries.size)) }
        }
    }

    fun onConfirmDelete() {
        val ids = _selectedEntries.map { entry -> entry.id }
        viewModelScope.launch { entriesRepository.deleteEntries(ids) }
        setSelectMode(false)
    }

    private fun setSelectMode(enable: Boolean) {
        _selectMode.value = enable
        _showCreateMenu.value = !enable

        if (enable) {
            _expandCreateMenu.value = false
            _title.value = selectedEntries.size.toString()
        } else {
            _selectedEntries.clear()
            viewModelScope.launch { eventChannel.send(Event.SelectedEntriesCleared) }
            list.let { _title.value = it.title }
        }
    }

    private fun determineBackPressedCallbackEnabled() {
        _backPressedCallbackEnabled.value = _expandCreateMenu.value == true || _selectMode.value == true
    }

    private fun getNextStatus(status: Status): Status {
        return when (status) {
            Status.TODO -> Status.IN_PROGRESS
            Status.IN_PROGRESS -> Status.DONE
            Status.DONE -> Status.TODO
        }
    }
}