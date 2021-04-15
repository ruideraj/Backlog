package com.ruideraj.backlog.lists

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.ListIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(private val listsRepository: ListsRepository) : ViewModel() {

    companion object {
        private const val TAG = "ListsViewModel"
    }

    private val _lists = MutableLiveData<List<BacklogList>>()
    val lists: LiveData<List<BacklogList>> = _lists

    private val _openListDialog = MutableSharedFlow<Bundle>()
    val openListDialog: SharedFlow<Bundle> = _openListDialog

    private val _showListDialogTitleError = MutableLiveData(false)
    val showListDialogTitleError: LiveData<Boolean> = _showListDialogTitleError

    private val _dismissListDialog = MutableSharedFlow<Unit>()
    val dismissListDialog: SharedFlow<Unit> = _dismissListDialog

    private val _openDeleteDialog = MutableSharedFlow<Bundle>()
    val openDeleteDialog: SharedFlow<Bundle> = _openDeleteDialog

    private var listBeingMoved: Long = -1

    init {
        viewModelScope.launch { listsRepository.loadLists().collect { lists -> _lists.value = lists } }
    }

    fun onClickCreateList() {
        val bundle = Bundle().apply {
            putInt(Constants.ARG_MODE, Constants.MODE_CREATE)
            putSerializable(Constants.ARG_ICON, ListIcon.LIST)  // Default icon
        }

        viewModelScope.launch { _openListDialog.emit(bundle) }
    }

    fun onClickEditList(position: Int) {
        val listToEdit = _lists.value!![position]
        val bundle = Bundle().apply {
            putInt(Constants.ARG_MODE, Constants.MODE_EDIT)
            putLong(Constants.ARG_LIST_ID, listToEdit.id)
            putString(Constants.ARG_TITLE, listToEdit.title)
            putSerializable(Constants.ARG_ICON, listToEdit.icon)
        }

        viewModelScope.launch { _openListDialog.emit(bundle) }
    }

    fun onClickDeleteList(position: Int) {
        val listToDelete = _lists.value!![position]
        val bundle = Bundle().apply { putParcelable(Constants.ARG_LIST, listToDelete) }
        viewModelScope.launch { _openDeleteDialog.emit(bundle) }
    }

    fun createList(title: String, icon: ListIcon) {
        if (title.isBlank()) {
            _showListDialogTitleError.value = true
        } else {
            viewModelScope.launch {
                _dismissListDialog.emit(Unit)
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
                    _dismissListDialog.emit(Unit)
                    listsRepository.editList(listId, title, icon)
                }
            }
        }
    }

    fun deleteList(listId: Long) = viewModelScope.launch { listsRepository.deleteList(listId) }

    fun moveListStarted(position: Int) {
        _lists.value?.let { listBeingMoved = it[position].id }
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