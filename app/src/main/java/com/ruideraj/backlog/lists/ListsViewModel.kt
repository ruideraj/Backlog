package com.ruideraj.backlog.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.local.ListItem
import com.ruideraj.backlog.data.ListsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(private val listsRepository: ListsRepository) : ViewModel() {

    sealed class Event {
        data class ShowCreateList(val defaultIcon: ListIcon) : Event()
        data class ShowEditList(val listId: Long, val title: String, val icon: ListIcon) : Event()
        object CloseListDialog : Event()
        data class ShowDeleteDialog(val list: BacklogList) : Event()
        data class GoToEntries(val list: BacklogList) : Event()
    }

    companion object {
        private const val TAG = "ListsViewModel"
    }

    private val _lists = MutableLiveData<List<ListItem>>()
    val lists: LiveData<List<ListItem>> = _lists

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val _showListDialogTitleError = MutableLiveData(false)
    val showListDialogTitleError: LiveData<Boolean> = _showListDialogTitleError

    private var listBeingMoved: Long = -1

    init {
        viewModelScope.launch { listsRepository.loadLists().collect { lists -> _lists.value = lists } }
    }

    fun onClickCreateList() {
        viewModelScope.launch { eventChannel.send(Event.ShowCreateList(ListIcon.LIST)) }
    }

    fun onClickEditList(position: Int) {
        val listItem = _lists.value!![position]
        viewModelScope.launch { eventChannel.send(
            Event.ShowEditList(listItem.list.id, listItem.list.title, listItem.list.icon)) }
    }

    fun onClickDeleteList(position: Int) {
        val listItem = _lists.value!![position]
        viewModelScope.launch { eventChannel.send(Event.ShowDeleteDialog(listItem.list)) }
    }

    fun onClickList(position: Int) {
        viewModelScope.launch {
            _lists.value?.let {
                val list = it[position].list
                viewModelScope.launch { eventChannel.send(Event.GoToEntries(list)) }
            }
        }
    }

    fun createList(title: String, icon: ListIcon) {
        if (title.isBlank()) {
            _showListDialogTitleError.value = true
        } else {
            viewModelScope.launch {
                eventChannel.send(Event.CloseListDialog)
                listsRepository.createList(title, icon)
            }
        }
    }

    fun editList(listId: Long, title: String, icon: ListIcon) {
        when {
            listId < 0 -> throw IllegalArgumentException("listId cannot be less than 0, listId: $listId")
            title.isBlank() -> _showListDialogTitleError.value = true
            else -> {
                viewModelScope.launch {
                    eventChannel.send(Event.CloseListDialog)
                    listsRepository.editList(listId, title, icon)
                }
            }
        }
    }

    fun deleteList(listId: Long) = viewModelScope.launch { listsRepository.deleteList(listId) }

    fun moveListStarted(position: Int) {
        _lists.value?.let { listBeingMoved = it[position].list.id }
    }

    fun moveListEnded(newPosition: Int) {
        viewModelScope.launch {
            listsRepository.moveList(listBeingMoved, newPosition)
        }
    }

    fun onDialogTitleTextChanged(input: String) {
        if (showListDialogTitleError.value == true && input.isNotBlank()) _showListDialogTitleError.value = false
    }

}