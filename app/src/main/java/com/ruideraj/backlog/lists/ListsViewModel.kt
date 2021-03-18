package com.ruideraj.backlog.lists

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.BacklogList
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

    val lists: LiveData<List<BacklogList>>
        get() = _lists
    private val _lists = MutableLiveData<List<BacklogList>>()

    val openListDialog: SharedFlow<Bundle>
        get() = _openListDialog
    private val _openListDialog = MutableSharedFlow<Bundle>()

    val showDialogTitleError: LiveData<Boolean>
        get() = _showDialogTitleError
    private val _showDialogTitleError = MutableLiveData(false)

    val dismissDialog: SharedFlow<Unit>
        get() = _dismissDialog
    private val _dismissDialog = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch { listsRepository.loadLists().collect { lists -> _lists.value = lists } }
    }

    fun onClickCreateList() {
        val bundle = Bundle().apply {
            putInt(ListDialogFragment.ARG_MODE, ListDialogFragment.MODE_CREATE)
            putSerializable(ListDialogFragment.ARG_ICON, ListIcon.LIST)  // Default icon
        }

        viewModelScope.launch { _openListDialog.emit(bundle) }
    }

    fun onClickEditList(position: Int) {
        val listToEdit = _lists.value!![position]
        val bundle = Bundle().apply {
            putInt(ListDialogFragment.ARG_MODE, ListDialogFragment.MODE_EDIT)
            putLong(ListDialogFragment.ARG_LIST_ID, listToEdit.listId)
            putString(ListDialogFragment.ARG_TITLE, listToEdit.title)
            putSerializable(ListDialogFragment.ARG_ICON, listToEdit.icon)
        }

        viewModelScope.launch { _openListDialog.emit(bundle) }
    }

    fun createList(title: String, icon: ListIcon) {
        if (title.isBlank()) {
            _showDialogTitleError.value = true
        } else {
            viewModelScope.launch {
                _dismissDialog.emit(Unit)
                listsRepository.createList(title, icon)
            }
        }
    }

    fun editList(listId: Long, title: String, icon: ListIcon) {
        if (listId < 0) {
            throw IllegalArgumentException("listId cannot be less than 0, listId: $listId")
        } else if (title.isBlank()) {
            _showDialogTitleError.value = true
        } else {
            viewModelScope.launch {
                _dismissDialog.emit(Unit)
                listsRepository.editList(listId, title, icon)
            }
        }
    }

    fun onDialogTitleTextChanged(input: String) {
        if (showDialogTitleError.value == true && input.isNotBlank()) _showDialogTitleError.value = false
    }

}